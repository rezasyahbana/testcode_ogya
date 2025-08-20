package com.volt.gen.config;

import com.volt.gen.util.Secure;
import com.voltage.securedata.enterprise.FPE;
import com.voltage.securedata.enterprise.VeException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import com.volt.gen.config.mybatis.MyBatisUtils;
import com.volt.gen.config.mybatis.entity.VoltageCustomFpeConfig;
import com.volt.gen.config.mybatis.mapper.VoltageCustomFpeConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@DependsOn("customLibraryContextConfiguration")
public class CustomFpeConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CustomFpeConfiguration.class);

    @Autowired
    CustomLibraryContextConfiguration customLibraryContextConfiguration;

    @FunctionalInterface
    public static interface VoltageFunction<T> {
        T apply(T value) throws VeException;
    }

    public ConcurrentHashMap<String, ThreadLocal<FPE>> customFpe;

    @PostConstruct
    public void init(){
        logger.info("Custom FPE - Creating...");
        customFpe = new ConcurrentHashMap<>();
        MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            List<VoltageCustomFpeConfig> listVoltageCustomFpeConfig = voltageCustomFpeConfigMapper.getAllVoltageCustomFpeConfig();

            for(VoltageCustomFpeConfig voltageCustomFpeConfig : listVoltageCustomFpeConfig){
                customFpe.put(voltageCustomFpeConfig.getFpeId(), ThreadLocal.withInitial(() -> {
                    try {
                        return customLibraryContextConfiguration.customLibCont.get(voltageCustomFpeConfig.getLibraryContextId())
                                .getFPEBuilder(Secure.decrypt(voltageCustomFpeConfig.getFormat()))
                                .setSharedSecret(Secure.decrypt(voltageCustomFpeConfig.getSharedSecret()))
                                .setIdentity(Secure.decrypt(voltageCustomFpeConfig.getIdentity()))
                        .build();
                    }catch(VeException e){
                        logger.error("Error creating FPE instance for ID: {}", voltageCustomFpeConfig.getFpeId(), e);
                        throw new RuntimeException(e);
                    }catch(Exception e){
                        logger.error("Error creating FPE instance for ID: {}", voltageCustomFpeConfig.getFpeId(), e);
                        throw new RuntimeException(e);
                    }
                }));
            }
            logger.info("Custom FPE - Create Completed.");
            logger.info("FPE Available is " + customFpe.size());
        }catch (Exception e){
            logger.error("Error creating FPE instance", e);
        }
    }

    public ConcurrentHashMap<String, ThreadLocal<FPE>> getCustomFpe(){
        return customFpe;
    }
    public void reload(){
        logger.info("Custom FPE - Reloading...");
        MyBatisUtils myBatisUtils = new MyBatisUtils();
            SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            List<VoltageCustomFpeConfig> listVoltageCustomFpeConfig = voltageCustomFpeConfigMapper.getAllVoltageCustomFpeConfig();

            for(VoltageCustomFpeConfig voltageCustomFpeConfig : listVoltageCustomFpeConfig){
                customFpe.put(voltageCustomFpeConfig.getFpeId(), ThreadLocal.withInitial(() -> {
                    try {
                        return customLibraryContextConfiguration.customLibCont.get(voltageCustomFpeConfig.getLibraryContextId())
                                .getFPEBuilder(Secure.decrypt(voltageCustomFpeConfig.getFormat()))
                                .setSharedSecret(Secure.decrypt(voltageCustomFpeConfig.getSharedSecret()))
                                .setIdentity(Secure.decrypt(voltageCustomFpeConfig.getIdentity()))
                        .build();
                    }catch(VeException e){
                        logger.error("Error Loading FPE instance for ID: {}", voltageCustomFpeConfig.getFpeId(), e);
                        throw new RuntimeException(e);
                    }catch(Exception e){
                        logger.error("Error Loading FPE instance for ID: {}", voltageCustomFpeConfig.getFpeId(), e);
                        throw new RuntimeException(e);
                    }
                }));
            }
            logger.info("Custom FPE - Reload Completed.");
            logger.info("FPE Available is " + customFpe.size());
        }catch (Exception e){
            logger.error("Error creating FPE instance", e);
        }
    }

    public void deleteAllCustomFpe() {
        logger.info("Custom FPE - Deleting...");
        if (customFpe != null) {
            customFpe.forEach((key, fpeThreadLocal) -> {
                FPE fpe = fpeThreadLocal.get();
                if (fpe != null) {
                    fpe.delete();
                }
                fpeThreadLocal.remove();
            });
            customFpe.clear();
        }
        logger.info("Custom FPE - Delete Completed.");
    }
}
