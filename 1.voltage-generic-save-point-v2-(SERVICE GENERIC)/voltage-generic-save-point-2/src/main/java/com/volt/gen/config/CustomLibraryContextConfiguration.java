package com.volt.gen.config;

import com.volt.gen.util.Secure;
import com.voltage.securedata.enterprise.LibraryContext;
import com.voltage.securedata.enterprise.VeException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import com.volt.gen.config.mybatis.MyBatisUtils;
import com.volt.gen.config.mybatis.entity.VoltageCustomLibraryContextConfig;
import com.volt.gen.config.mybatis.mapper.VoltageCustomLibraryContextConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Component
@DependsOn("datasourceInitialzing")
public class CustomLibraryContextConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CustomLibraryContextConfiguration.class);

    @Value("${config.voltage.TrustStorePath}")
    private String trustStorePath;

    @Value("${config.voltage.CachePath}")
    private String cachePath;

    @Value("${config.voltage.ClientId}")
    private String clientId;

    @Value("${config.voltage.ClientIdVersion}")
    private String clientIdVersion;

    public static HashMap<String, LibraryContext> customLibCont;

    @PostConstruct
    public void init(){
        logger.info("Custom Library Context - Creating...");
        customLibCont = new HashMap<>();
        MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            List<VoltageCustomLibraryContextConfig> listVoltageCustomLibraryContextConfig = voltageCustomLibraryContextConfigMapper.getAllVoltageCustomLibraryContextConfig();

            for(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig : listVoltageCustomLibraryContextConfig){
                LibraryContext library = new LibraryContext.Builder()
                        .setPolicyURL(Secure.decrypt(voltageCustomLibraryContextConfig.getPolicyUrl()))
                        .setFileCachePath(cachePath)
                        .setTrustStorePath(trustStorePath)
                        .setClientIdProduct(clientId, clientIdVersion)
                        .build();
                customLibCont.put(voltageCustomLibraryContextConfig.getLibraryContextId(),library);
            }
            logger.info("Custom Library Context - Create Completed.");
            logger.info("Library Available is {}" , customLibCont.size());
        }catch (VeException e){
            logger.error("Custom Library Context - Create Failed due to VeException.", e);
            throw new RuntimeException("Failed to create context", e);
        } catch (Exception e) {
            logger.error("Custom Library Context - Unexpected failure.", e);
            throw new RuntimeException("Unexpected error during context creation", e);
        }
    }

    public HashMap<String, LibraryContext> getCustomLibCont() {
        return customLibCont;
    }

    public void reload(){
        logger.info("Custom Library Context - Reloading...");
        MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            List<VoltageCustomLibraryContextConfig> listVoltageCustomLibraryContextConfig = voltageCustomLibraryContextConfigMapper.getAllVoltageCustomLibraryContextConfig();

            for(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig : listVoltageCustomLibraryContextConfig){
                LibraryContext library = new LibraryContext.Builder()
                        .setPolicyURL(Secure.decrypt(voltageCustomLibraryContextConfig.getPolicyUrl()))
                        .setFileCachePath(cachePath)
                        .setTrustStorePath(trustStorePath)
                        .setClientIdProduct(clientId, clientIdVersion)
                        .build();
                if(customLibCont.get(voltageCustomLibraryContextConfig.getLibraryContextId()) != null)
                    customLibCont.get(voltageCustomLibraryContextConfig.getLibraryContextId()).delete();
                customLibCont.put(voltageCustomLibraryContextConfig.getLibraryContextId(),library);
            }
            logger.info("Custom Library Context - Reload Completed.");
            logger.info("Library Available is {}" , customLibCont.size());
        } catch (VeException e){
            logger.error("Custom Library Context - Create Failed due to VeException.", e);
            throw new RuntimeException("Failed to create context", e);
        } catch (Exception e) {
            logger.error("Custom Library Context - Unexpected failure.", e);
            throw new RuntimeException("Unexpected error during context creation", e);
        }
    }

    public void deleteAllCustomLibrary(){
        logger.info("Custom Library Context - Deleting...");
        if(customLibCont != null){
            customLibCont.forEach((key, libraryContext) -> {
                if(libraryContext != null){
                    libraryContext.delete();
                }
            });
            customLibCont.clear();
        }
        logger.info("Custom Library Context - Delete Completed.");
    }
}
