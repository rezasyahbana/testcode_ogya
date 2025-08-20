package org.tk.sda.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.tk.sda.config.mybatis.MyBatisUtils;
import org.tk.sda.config.mybatis.entity.VoltageCustomFpeConfig;
import org.tk.sda.config.mybatis.entity.VoltageCustomLibraryContextConfig;
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
public class CustomLibraryContextService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    public List<VoltageCustomLibraryContextConfig> getAllVoltageCustomLibraryContextConfig(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            List<VoltageCustomLibraryContextConfig> listVoltageCustomLibraryContextConfig = voltageCustomLibraryContextConfigMapper.getAllVoltageCustomLibraryContextConfig();
            for(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig : listVoltageCustomLibraryContextConfig){
                voltageCustomLibraryContextConfig.setPolicyUrl(Secure.decrypt(voltageCustomLibraryContextConfig.getPolicyUrl()));
            }
            return listVoltageCustomLibraryContextConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public void setVoltageCustomLibraryContextConfig(List<VoltageCustomLibraryContextConfig> listVoltageCustomLibraryContextConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            for(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig : listVoltageCustomLibraryContextConfig){
                //checking ID exist or not
                if (voltageCustomLibraryContextConfigMapper.getVoltageCustomLibraryContextConfigById(voltageCustomLibraryContextConfig.getLibraryContextId()) != null) {
                    //turn into exception ID already registered
                    throw new DuplicateIdException("ID " + voltageCustomLibraryContextConfig.getLibraryContextId() + " already registered");
                }
                voltageCustomLibraryContextConfig.setPolicyUrl(Secure.encrypt(voltageCustomLibraryContextConfig.getPolicyUrl()));
                voltageCustomLibraryContextConfigMapper.insertVoltageCustomLibraryContextConfig(voltageCustomLibraryContextConfig);
            }
            session.commit();
        }catch (Exception e){
            logger.error("Insert Library Context failed", e);
            throw new RuntimeException(e);
        }
    }

    public void updateVoltageCustomLibraryContextConfig(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            //checking ID exist or not
            if (voltageCustomLibraryContextConfigMapper.getVoltageCustomLibraryContextConfigById(voltageCustomLibraryContextConfig.getLibraryContextId()) == null) {
                //turn into exception ID not registered
                throw new IdDoesNotExistException("ID " + voltageCustomLibraryContextConfig.getLibraryContextId() + " not exist");
            }
            voltageCustomLibraryContextConfig.setPolicyUrl(Secure.encrypt(voltageCustomLibraryContextConfig.getPolicyUrl()));
            voltageCustomLibraryContextConfigMapper.updateVoltageCustomLibraryContextConfig(voltageCustomLibraryContextConfig);
            session.commit();
        }catch (Exception e){
            logger.error("Update failed for ID {}", voltageCustomLibraryContextConfig.getLibraryContextId(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteVoltageCustomLibraryContextConfig(String libraryContextId){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            VoltageCustomLibraryContextConfigMapper voltageCustomLibraryContextConfigMapper = myBatisUtils.createMapper(VoltageCustomLibraryContextConfigMapper.class,session);
            //checking ID exist or not
            if(voltageCustomLibraryContextConfigMapper.getVoltageCustomLibraryContextConfigById(libraryContextId) == null){
                //turn into exception ID not registered
                throw new IdDoesNotExistException("ID " + libraryContextId + " not exist");
            }
            VoltageCustomFpeConfigMapper voltageCustomFpeConfigMapper = myBatisUtils.createMapper(VoltageCustomFpeConfigMapper.class,session);
            List<VoltageCustomFpeConfig> listVoltageCustomFpeConfig = voltageCustomFpeConfigMapper.getVoltageCustomFpeConfigByLibraryContextId(libraryContextId);

            VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);

            if(!listVoltageCustomFpeConfig.isEmpty()){
                for(VoltageCustomFpeConfig voltageCustomFpeConfig : listVoltageCustomFpeConfig){
                    //delete transformDetail with FpeId
                    if(!voltageTransformDetailConfigMapper.getVoltageTransformDetailConfigByFpeId(voltageCustomFpeConfig.getFpeId()).isEmpty())
                        voltageTransformDetailConfigMapper.deleteVoltageTransformDetailConfigByFpeId(voltageCustomFpeConfig.getFpeId());
                }
                //delete FpeId by LibraryContextId
                voltageCustomFpeConfigMapper.deleteVoltageCustomFpeLoadConfigByLibraryContextId(libraryContextId);
            }

            //delete LibraryContext by LibraryContextId
            voltageCustomLibraryContextConfigMapper.deleteVoltageCustomLibraryContextConfig(libraryContextId);
            session.commit();
            logger.info("Successfully deleted config for ID {}", libraryContextId);
        }catch (Exception e){
            logger.error("Delete failed for Library Context ID {}", libraryContextId, e);
            throw new RuntimeException(e);
        }
    }


}
