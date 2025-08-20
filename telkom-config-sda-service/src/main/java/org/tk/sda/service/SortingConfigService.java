package org.tk.sda.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.tk.sda.config.mybatis.MyBatisUtils;
import org.tk.sda.config.mybatis.entity.SortingConfig;
import org.tk.sda.config.mybatis.mapper.SortingConfigMapper;
import org.tk.sda.config.util.DuplicateIdException;
import org.tk.sda.config.util.IdDoesNotExistException;
import org.tk.sda.config.util.Secure;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SortingConfigService {

    private Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    public List<SortingConfig> getAllSortingConfig(){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);
            List<SortingConfig> listSortingConfig = sortingConfigMapper.getAllSortingConfig();
            for(SortingConfig sortingConfig : listSortingConfig){
                sortingConfig.setSortByFieldName(Secure.decrypt(sortingConfig.getSortByFieldName()));
            }
            return listSortingConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public SortingConfig getSortingConfigById(String id){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);
            SortingConfig sortingConfig = sortingConfigMapper.getSortingConfigById(id);
            sortingConfig.setSortByFieldName(Secure.decrypt(sortingConfig.getSortByFieldName()));
            return sortingConfig;
        }catch (Exception e){
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    public void setSortingConfig(List<SortingConfig> listSortingConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);

            for(SortingConfig sortingConfig : listSortingConfig){
                //validation ID
                if(sortingConfigMapper.getSortingConfigById(sortingConfig.getId()) != null){
                    //throw exception id already exist
                    throw new DuplicateIdException("ID " + sortingConfig.getId() + " already registered");
                }
                sortingConfig.setSortByFieldName(Secure.encrypt(sortingConfig.getSortByFieldName()));
                sortingConfigMapper.insertSortingConfig(sortingConfig);
            }
            session.commit();
        }catch (Exception e){
            logger.error("Insert Sorting Config failed", e);
            throw new RuntimeException(e);
        }
    }

    public void updateSortingConfig(SortingConfig sortingConfig){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);
            //validation ID
            if(sortingConfigMapper.getSortingConfigById(sortingConfig.getId()) == null){
                //throw exception id doesnt not exist
                throw new IdDoesNotExistException("ID " + sortingConfig.getId() + " not exist");
            }

            sortingConfig.setSortByFieldName(Secure.encrypt(sortingConfig.getSortByFieldName()));
            sortingConfigMapper.updateSortingConfig(sortingConfig);

            session.commit();
        }catch (Exception e){
            logger.error("Udpate Sorting Config failed", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteSortingConfig(String id){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)){
            SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);
            //validation ID
            if(sortingConfigMapper.getSortingConfigById(id) == null){
                //throw exception id doesnt not exist
                throw new IdDoesNotExistException("ID " + id + " not exist");
            }

            sortingConfigMapper.deleteSortingConfigById(id);
            session.commit();
        }catch (Exception e){
            logger.error("delete Sorting Config failed", e);
            throw new RuntimeException(e);
        }
    }
}
