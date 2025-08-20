package com.volt.gen.service.Impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.telkom.logging.avro.schema.LogObject;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volt.gen.LRUCacheComponent;
import com.volt.gen.config.CustomFpeConfiguration;
import com.volt.gen.config.VoltageLibraryLoad;
import com.volt.gen.config.mybatis.entity.SortingConfig;
import com.volt.gen.config.mybatis.entity.VoltageTransformDetailConfig;
import com.volt.gen.exception.VoltGenException;
import com.volt.gen.service.KafkaProducerService;
import com.volt.gen.service.VoltageFpeService;
import com.volt.gen.util.JsonPathJsonOrgUtil;
import com.volt.gen.util.LogObjectWrapper;
import com.voltage.securedata.enterprise.FPE;

@Service
public class VoltageFpeServiceImpl implements VoltageFpeService {
    private Logger logger = LoggerFactory.getLogger("Voltage FPE Service");

    @Autowired
    Environment environment;

    @Autowired
    VoltageLibraryLoad voltageLibraryLoad;

    @Autowired
    CustomFpeConfiguration customFpeConfiguration;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public String transformData(String transformId, String body) {

        logger.info("Transform Service - Transform ID is {}", transformId);

        List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig = LRUCacheComponent.getInstance()
                .getListVoltageTransformDetailConfig(transformId);

        String responseBody = body;

        if (!listVoltageTransformDetailConfig.isEmpty()) {
            boolean isProccessed = checkBody(body);
            if (isProccessed) {
                logger.info("Transform Service - Transforming...");
                responseBody = transform(transformId, responseBody, listVoltageTransformDetailConfig);
                logger.info("Transform Service - Transform Completed.");
            } else {
                logger.info("Transform Service - Body is Empty or Not Correction in Format");
                logger.info("Transform Service - Transform Not Confirmed.");
                logger.info("Transform Service - By Passing...");
                logger.info("");
            }
        } else {
            logger.info("Transform Service  - No Configuration Found With ID {}", transformId);
            logger.info("Transform Service  - Transform Not Confirmed.");
            logger.info("Transform Service  - By Passing...");
            logger.info("");
        }
        return responseBody;
    }

    @Override
    public String checkService(String data) {
        String encryptedData = "";
        try {
            Optional<ThreadLocal<FPE>> anyFpeThreadLocal = customFpeConfiguration.customFpe.values()
                    .stream()
                    .findFirst();
            if (anyFpeThreadLocal.isPresent()) {
                FPE fpe = anyFpeThreadLocal.get().get();
                if (fpe != null) {
                    encryptedData = fpe.protect(data);
                }
            } else {
                logger.warn("No FPE instances found in the configuration.");
            }
        } catch (Exception e) {
            logger.error("Checking Service Error", e);
        }
        return encryptedData;
    }

    private String transform(String transformId, String body,
            List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig) {
        JsonPathJsonOrgUtil util = new JsonPathJsonOrgUtil(body, voltageLibraryLoad, customFpeConfiguration);
        for (VoltageTransformDetailConfig voltageTransformDetailConfig : listVoltageTransformDetailConfig) {
            var low = LogObjectWrapper.builder().hostname(request.getRemoteAddr()).transformId(transformId)
                    .type(voltageTransformDetailConfig.getTransformType());
            try {
                util.transformValue(voltageTransformDetailConfig.getJsonPathFieldName(),
                        voltageTransformDetailConfig.getTransformType(), voltageTransformDetailConfig.getFpeId());
            } catch (VoltGenException ex) {
                low.message(ex.getMessage()).status("failed");
            } finally {
                var logObject = low.build().toLogObject();
                if (logObject == null) {
                    logger.warn("Attempted to send null log object to Kafka. Skipping.");
                } else {
                    kafkaProducerService.send(logObject);
                    String rawLog = convertToRawString(logObject);  // Implement this method

                    if (rawLog != null) {
                        kafkaProducerService.sendRaw(rawLog);
                    }
                }
            }

        }

        SortingConfig sortingConfig = LRUCacheComponent.getInstance().getSortingConfig(transformId);

        if (sortingConfig != null && util.isArray() && sortingConfig.getIsSort()) {
            logger.debug("Transform Service  - Sorting By Field {}", sortingConfig.getSortByFieldName());
            if (sortingConfig.getAscdesc().equalsIgnoreCase("ASC"))
                return util.sorting(sortingConfig.getSortByFieldName(), true);
            if (sortingConfig.getAscdesc().equalsIgnoreCase("DESC"))
                return util.sorting(sortingConfig.getSortByFieldName(), false);
        }
        return util.getJsonString();
    }

    private boolean checkBody(String body) {
        try {
            if (body == null || body.trim().isEmpty()) {
                return false;
            }

            String trimmedBody = body.trim();

            if ("{}".equals(trimmedBody) || "[{}]".equals(trimmedBody)) {
                return false;
            }

            new ObjectMapper().readTree(trimmedBody);
        } catch (JsonProcessingException e) {
            return false;
        }

        return true;
    }

    private String convertToRawString(LogObject logObject) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Schema schema = logObject.getSchema(); // Avro schema
            DatumWriter<LogObject> writer = new SpecificDatumWriter<>(schema);
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, out);
            writer.write(logObject, encoder);
            encoder.flush();
            return out.toString();
        } catch (IOException e) {
            logger.error("Failed to convert Avro object to JSON", e);
            return null;
        }
    }

}
