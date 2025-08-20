package org.tk.sda.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.tk.sda.config.mybatis.MyBatisUtils;
import org.tk.sda.config.mybatis.entity.VoltageCustomFpeConfig;
import org.tk.sda.config.mybatis.mapper.VoltageCustomFpeConfigMapper;
import org.tk.sda.config.mybatis.mapper.VoltageCustomLibraryContextConfigMapper;
import org.tk.sda.config.mybatis.mapper.VoltageTransformDetailConfigMapper;
import org.tk.sda.config.util.DuplicateIdException;
import org.tk.sda.config.util.IdDoesNotExistException;
import org.tk.sda.config.util.Secure;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VoltageCustomFpeConfigService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    public List<VoltageCustomFpeConfig> getAllVoltageCustomFpeConfig(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            List<VoltageCustomFpeConfig> listVoltageCustomFpeConfig = voltageCustomFpeConfigMapper.getAllVoltageCustomFpeConfig();
            for(VoltageCustomFpeConfig voltageCustomFpeConfig : listVoltageCustomFpeConfig){
                voltageCustomFpeConfig.setIdentity(Secure.decrypt(voltageCustomFpeConfig.getIdentity()));
                voltageCustomFpeConfig.setSharedSecret(Secure.decrypt(voltageCustomFpeConfig.getSharedSecret()));
                voltageCustomFpeConfig.setFormat(Secure.decrypt(voltageCustomFpeConfig.getFormat()));
            }
            return listVoltageCustomFpeConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public void insertVoltageCustomFpeConfig(List<VoltageCustomFpeConfig> listVoltageCustomFpeConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            for(VoltageCustomFpeConfig voltageCustomFpeConfig : listVoltageCustomFpeConfig){
                //validation ID
                if(voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigById(voltageCustomFpeConfig.getFpeId()) != null){
                    //turn into exception ID already registered
                    throw new DuplicateIdException("FPE ID " + voltageCustomFpeConfig.getFpeId() + " already registered");
                }
                VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
                if(voltageCustomLibraryContextConfigMapper.getVoltageCustomLibraryContextConfigById(voltageCustomFpeConfig.getLibraryContextId()) == null){
                    //throw error cause library context does not exist
                    throw new IdDoesNotExistException("Library Context ID " + voltageCustomFpeConfig.getLibraryContextId() + " Does Not Exist");
                }
                voltageCustomFpeConfig.setIdentity(Secure.encrypt(voltageCustomFpeConfig.getIdentity()));
                voltageCustomFpeConfig.setSharedSecret(Secure.encrypt(voltageCustomFpeConfig.getSharedSecret()));
                voltageCustomFpeConfig.setFormat(Secure.encrypt(voltageCustomFpeConfig.getFormat()));
                voltageCustomFpeConfigMapper.insertVoltageCustomFpeLoadConfig(voltageCustomFpeConfig);
            }
            session.commit();
        }catch (Exception e){
            logger.error("Insert FPE failed", e);
            throw new RuntimeException(e);
        }
    }

    public void updateVoltageCustomFpeConfig(VoltageCustomFpeConfig voltageCustomFpeConfig){
        long startDt = System.currentTimeMillis();
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            //Validation ID
            if(voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigById(voltageCustomFpeConfig.getFpeId()) == null) {
                //throw error cause FPE ID does not exist
                throw new IdDoesNotExistException("FPE ID " + voltageCustomFpeConfig.getFpeId() + " Does Not Exist");
            }
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            if(voltageCustomLibraryContextConfigMapper.getVoltageCustomLibraryContextConfigById(voltageCustomFpeConfig.getLibraryContextId()) == null){
                //throw error cause library context does not exist
                throw new IdDoesNotExistException("Library Context ID " + voltageCustomFpeConfig.getLibraryContextId() + " Does Not Exist");
            }
            voltageCustomFpeConfig.setIdentity(Secure.encrypt(voltageCustomFpeConfig.getIdentity()));
            voltageCustomFpeConfig.setSharedSecret(Secure.encrypt(voltageCustomFpeConfig.getSharedSecret()));
            voltageCustomFpeConfig.setFormat(Secure.encrypt(voltageCustomFpeConfig.getFormat()));
            voltageCustomFpeConfigMapper.updateVoltageCustomFpeLoadConfig(voltageCustomFpeConfig);
            session.commit();
        }catch (Exception e){
            logger.error("Update FPE failed", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteVoltageCustomFpeConfig(String fpeId){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            //checking ID exist or not
            if(voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigById(fpeId) == null){
                //turn into exception ID already exist
                throw new IdDoesNotExistException("FPE ID " + fpeId + " Does Not Exist");
            }
            //delete transformDetail with FpeId
            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
            if(!voltageTransformDetailConfigMapper.getVoltageTransformDetailConfigByFpeId(fpeId).isEmpty())
                voltageTransformDetailConfigMapper.deleteVoltageTransformDetailConfigByFpeId(fpeId);

            //delete Fpe with fpeId
            voltageCustomFpeConfigMapper.deleteVoltageCustomFpeLoadConfig(fpeId);
            session.commit();
        }catch (Exception e){
            logger.error("Delete failed for FPE ID {}", fpeId, e);
            throw new RuntimeException(e);
        }
    }
}
