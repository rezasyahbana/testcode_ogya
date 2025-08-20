package org.tk.sda.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.tk.sda.config.mybatis.MyBatisUtils;
import org.tk.sda.config.mybatis.entity.VoltageTransformDetailConfig;
import org.tk.sda.config.mybatis.mapper.VoltageCustomFpeConfigMapper;
import org.tk.sda.config.mybatis.mapper.VoltageTransformDetailConfigMapper;
import org.tk.sda.config.util.DuplicateIdException;
import org.tk.sda.config.util.IdDoesNotExistException;
import org.tk.sda.config.util.Secure;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VoltageTransformDetailConfigService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");


    public List<VoltageTransformDetailConfig> getAllVoltageTransformDetailConfig(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig = voltageTransformDetailConfigMapper.getAllVoltageTransformDetailConfig();
            for(VoltageTransformDetailConfig voltageTransformDetailConfig : listVoltageTransformDetailConfig){
                voltageTransformDetailConfig.setJsonPathFieldName(Secure.decrypt(voltageTransformDetailConfig.getJsonPathFieldName()));
            }
            return listVoltageTransformDetailConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public List<VoltageTransformDetailConfig> getVoltageTransformDetailConfigById(String jsonTransformId){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig = voltageTransformDetailConfigMapper.getVoltageTransformDetailConfig(jsonTransformId);
            for(VoltageTransformDetailConfig voltageTransformDetailConfig : listVoltageTransformDetailConfig){
                voltageTransformDetailConfig.setJsonPathFieldName(Secure.decrypt(voltageTransformDetailConfig.getJsonPathFieldName()));
            }
            return listVoltageTransformDetailConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public void insertVoltageTransformDetailConfig(List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            for(VoltageTransformDetailConfig voltageTransformDetailConfig : listVoltageTransformDetailConfig){
                VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
                if(voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigById(voltageTransformDetailConfig.getFpeId()) == null){
                    //turn into exception FPE ID does not exist
                    throw new IdDoesNotExistException("FPE ID " + voltageTransformDetailConfig.getFpeId() + " does not exist");
                }
                voltageTransformDetailConfig.setJsonPathFieldName(Secure.encrypt(voltageTransformDetailConfig.getJsonPathFieldName()));
                voltageTransformDetailConfigMapper.insertVoltageTransformDetailConfig(voltageTransformDetailConfig);
            }
            session.commit();
        }catch (Exception e){
            logger.error("Insert Transform Detail Config failed", e);
            throw new RuntimeException(e);
        }
    }

    public void updateVoltageTransformDetailConfig(VoltageTransformDetailConfig voltageTransformDetailConfig){
        long startDt = System.currentTimeMillis();
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            voltageTransformDetailConfig.setJsonPathFieldName(Secure.encrypt(voltageTransformDetailConfig.getJsonPathFieldName()));
            //validation ID
            if(voltageTransformDetailConfigMapper.getVoltageTransformDetaildConfigByIdAndJsonPathName(voltageTransformDetailConfig) == null) {
                //turn into exception ID not exist
                throw new IdDoesNotExistException("Transform Detail Config ID" + voltageTransformDetailConfig.getJsonTransformId() + " does not exist");
            }
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            if(voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigById(voltageTransformDetailConfig.getFpeId()) == null){
                //turn into exception FPE ID does not exist
                throw new IdDoesNotExistException("FPE ID " + voltageTransformDetailConfig.getFpeId() + " does not exist");
            }
            voltageTransformDetailConfigMapper.updateVoltageTransformDetailConfig(voltageTransformDetailConfig);
            session.commit();
        }catch (Exception e){
            logger.error("Update Transform Detail Config failed", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteVoltageTransformDetailConfigById(String jsonTransformId){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            //validation ID
            if(voltageTransformDetailConfigMapper.getVoltageTransformDetailConfig(jsonTransformId) == null){
                throw new IdDoesNotExistException("Transform Detail Config ID" + jsonTransformId + " does not exist");
            }

            voltageTransformDetailConfigMapper.deleteVoltageTransformDetailConfigById(jsonTransformId);
            session.commit();
        }catch (Exception e){
            logger.error("Delete failed for Transform Detail Config ID {}", jsonTransformId, e);
            throw new RuntimeException(e);
        }
    }

    public void deleteVoltageTransformDetailConfigByIdAndName(String jsonTransformId, String jsonPathFieldName){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            //validation ID
            if(voltageTransformDetailConfigMapper.getVoltageTransformDetailConfig(jsonTransformId) == null){
                throw new IdDoesNotExistException("Transform Detail Config ID" + jsonTransformId + " does not exist");
            }

            String SecureJsonPathFieldName = Secure.encrypt(jsonPathFieldName);
            voltageTransformDetailConfigMapper.deleteVoltageTransformDetailConfigByIdAndName(jsonTransformId,SecureJsonPathFieldName);
            session.commit();
        }catch (Exception e){
            logger.error("Delete failed for Transform Detail Config ID {}", jsonTransformId, e);
            throw new RuntimeException(e);
        }
    }

}
