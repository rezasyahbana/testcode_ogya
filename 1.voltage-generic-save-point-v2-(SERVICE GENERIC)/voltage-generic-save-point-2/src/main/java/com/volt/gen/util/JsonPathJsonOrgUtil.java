package com.volt.gen.util;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonOrgMappingProvider;
import com.volt.gen.exception.VoltGenException;
import com.voltage.securedata.enterprise.FPE;
import com.voltage.securedata.enterprise.VeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.volt.gen.config.CustomFpeConfiguration;
import com.volt.gen.config.VoltageLibraryLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

public class JsonPathJsonOrgUtil {
    private DocumentContext documentContext;
    private final ParseContext parseContext;
    private VoltageLibraryLoad voltageLibraryLoad;
    private CustomFpeConfiguration customFpeConfiguration;

    private ParseContext pathReader;

//    @PostConstruct
//    private void init() {
//        pathReader = JsonPath.using(Configuration.builder()
//                .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS).build());
//    }

    private static final Logger logger = LoggerFactory.getLogger(JsonPathJsonOrgUtil.class);

    @Autowired
    public JsonPathJsonOrgUtil(String jsonString, VoltageLibraryLoad voltageLibraryLoad, CustomFpeConfiguration customFpeConfiguration) {
        Configuration configuration = Configuration.builder()
                .jsonProvider(new JsonOrgJsonProvider())
                .mappingProvider(new JsonOrgMappingProvider())
                .options(SUPPRESS_EXCEPTIONS)
                .build();
        parseContext = JsonPath.using(configuration);
        pathReader = JsonPath.using(Configuration.builder().options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS).build());
        documentContext = parseContext.parse(jsonString);
        this.voltageLibraryLoad = voltageLibraryLoad;
        this.customFpeConfiguration = customFpeConfiguration;
    }

    public void setDocumentContext(Object object) {
        documentContext = parseContext.parse(object);
    }

    public boolean isArray() {
        Object obj = documentContext.read("$");
        return obj instanceof JSONArray;
    }

    public boolean checkIsArray(String path) {
        Object obj = documentContext.read(path);
        if (obj instanceof JSONArray) {
            JSONArray list = (JSONArray) obj;
            for (Object element : list) {
                if (!(element instanceof String)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public String get(String path) {
        Object obj = documentContext.read(path);
        if (obj instanceof Integer) {
            return ((Integer) obj).toString();
        } else if (obj instanceof Double) {
            return obj.toString();
        } else if (obj instanceof List) {
            return obj.toString();
        } else {
            return obj.toString();
        }
    }

    public List<?> gets(String path) {
        Object obj = documentContext.read(path);
        if (obj instanceof JSONArray) {
            List<Object> list = new ArrayList<>();
            for (Object objx : (JSONArray) obj) {
                list.add(objx);
            }
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    public void set(String path, Object value) {
        documentContext.set(path, value);
    }

    public String getJsonString() {
        return getJsonString(false);
    }

    public String getJsonString(boolean prettyPrint) {
        if (prettyPrint)
            return JsonFormatter.prettyPrint(documentContext.jsonString());
        else
            return documentContext.jsonString();
    }

    public String sorting(String path, boolean isAsc) {
        if (isArray()) {
            JSONArray jsonArray = new JSONArray(getJsonString());
            JSONArray sortedJsonArray = new JSONArray();
            List<JSONObject> list = new ArrayList();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i));
            }
            Collections.sort(list, new Comparator() {
                @Override
                public int compare(Object a, Object b) {
                    String str1 = "";
                    String str2 = "";
                    try {
                        str1 = parseContext.parse(a.toString()).read(path);
                        str2 = parseContext.parse(b.toString()).read(path);
                    } catch (JSONException e) {
                        logger.warn(e.getMessage());
                    }
                    return (isAsc) ? str1.compareTo(str2) : (str1.compareTo(str2) * -1);
                }
            });
            for (int i = 0; i < jsonArray.length(); i++) {
                sortedJsonArray.put(list.get(i));
            }
            return JsonFormatter.prettyPrint(sortedJsonArray.toString());
        } else {
            return getJsonString(true);
        }
    }

    public void transformValue(String path, String transformType, String transformId) throws VoltGenException {
        String[] pathSegments = path.split("\\.");
        transformValueRecursively(pathSegments, 0, "", transformType, transformId);
    }

    private void transformValueRecursively(String[] pathSegments, int index, String previousSegment, String transformType, String transformId) throws VoltGenException {
        if (index >= pathSegments.length) return;

        String currentSegment = previousSegment + pathSegments[index];

        if (checkIsArray(currentSegment)) {
            List<Object> listObject = (List<Object>) gets(currentSegment + "[*]");
            index++;
            for (int i = 0; i < listObject.size(); i++) {
                transformValueRecursively(pathSegments, index, currentSegment + "[" + i + "].", transformType, transformId);
            }
        } else {
            if (index == pathSegments.length - 1) {
                try {
                    if (customFpeConfiguration.customFpe.get(transformId) == null) {
                        voltageLibraryLoad.reload();
                    }

                    ThreadLocal<FPE> fpeThreadLocal = customFpeConfiguration.customFpe.get(transformId);
                    if (fpeThreadLocal != null) {
                        FPE fpe = fpeThreadLocal.get();
                    if (fpe != null) {
                        CustomFpeConfiguration.VoltageFunction<String> fpeFunc = switch (transformType) {
                            case "encrypt" -> fpe::protect;
                            case "decrypt" -> fpe::access;
                            case "mask" -> fpe::accessMasked;
                            default -> null;
                        };

                        if (fpeFunc != null) {
                            deepTransformPathValue(currentSegment, fpeFunc);
                        } else {
                            logger.warn("Invalid transform type: {}", transformType);
                        }
                    } else {
                        logger.warn("Transform Plugin - FPE not listed for ID: {}", transformId);
                    }
                    }
                } catch (VeException e) {
                    //logger.error("Transformation error at {} : {}", currentSegment, e.getMessage());
                    throw new VoltGenException("failed transforming value: ", e);
                }
            } else {
                index++;
                transformValueRecursively(pathSegments, index, currentSegment + ".", transformType, transformId);
            }
        }
    }

    private void deepTransformPathValue(String path, CustomFpeConfiguration.VoltageFunction<String> fpeFunc) throws VeException {
        Object vObj = documentContext.read(path);

        if (vObj instanceof JSONObject jsonObject) {
            Map<String, Object> map = jsonObject.toMap();
            Set<String> children = findAllKeys(map, path);
            for (String child : children) {
                deepTransformPathValue(child, fpeFunc);
            }
        } else if(vObj instanceof JSONArray jsonArray){
            for (int i = 0; i < jsonArray.length(); i++) {
                deepTransformPathValue(path + "[" + i + "]", fpeFunc);
            }
        }
        else if (vObj instanceof String || vObj instanceof Number) {
            String oldVal = String.valueOf(vObj);
            String newVal = fpeFunc.apply(oldVal);
            documentContext.set(path, newVal);
        } else {
            logger.warn("Skipped non-transformable type at {}: {}", path, vObj.getClass().getSimpleName());
        }
    }

    private Set<String> findAllKeys(Map<?, ?> json, String currPath) {
        List<String> readPaths = pathReader.parse(json).read("$.*");
        Set<String> resultingPath = new HashSet<>();

        for (String path : readPaths) {
            resultingPath.add(currPath + path.replace("$", ""));
        }

        DocumentContext docCtx = JsonPath.parse(json);
        for (String path : readPaths) {
            Object vObj = docCtx.read(path);
            if (vObj instanceof Map<?, ?> vMap) {
                resultingPath.remove(currPath + path.replace("$", ""));
                resultingPath.addAll(findAllKeys(vMap, currPath + path.replace("$", "")));
            }
        }
        return resultingPath;
    }
}

