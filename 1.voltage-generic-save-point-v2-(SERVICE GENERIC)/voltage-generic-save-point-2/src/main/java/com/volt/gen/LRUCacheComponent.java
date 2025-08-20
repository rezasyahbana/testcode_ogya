package com.volt.gen;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.volt.gen.util.Secure;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import com.volt.gen.config.mybatis.MyBatisUtils;
import com.volt.gen.config.mybatis.entity.SortingConfig;
import com.volt.gen.config.mybatis.entity.VoltageTransformDetailConfig;
import com.volt.gen.config.mybatis.mapper.SortingConfigMapper;
import com.volt.gen.config.mybatis.mapper.VoltageTransformDetailConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LRUCacheComponent {
    private static final Logger logger = LoggerFactory.getLogger(LRUCacheComponent.class);

    private Cache<String, SortingConfig> sortingConfigCache;
    private Cache<String, List<VoltageTransformDetailConfig>> listVoltageTransformDetailConfigCache;
    private static AtomicReference<LRUCacheComponent> _instance =new AtomicReference<>();

    public static LRUCacheComponent getInstance() {
        LRUCacheComponent localRef = _instance.get();
        if (localRef == null) {
            localRef  = new LRUCacheComponent();
            if(_instance.compareAndSet(null,localRef)){
                localRef = _instance.get();
            }
        }
        return localRef;
    }

    public void reload(){
        sortingConfigCache.invalidateAll();
        listVoltageTransformDetailConfigCache.invalidateAll();
    }

    private LRUCacheComponent() {
        logger.info("CACHE INITIATE");

        if (sortingConfigCache != null) {
            sortingConfigCache.invalidateAll();
            sortingConfigCache = null;
        }
        sortingConfigCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

        if (listVoltageTransformDetailConfigCache != null) {
            listVoltageTransformDetailConfigCache.invalidateAll();
            listVoltageTransformDetailConfigCache = null;
        }
        listVoltageTransformDetailConfigCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    public List<VoltageTransformDetailConfig> getListVoltageTransformDetailConfig(String transformId){
        List<VoltageTransformDetailConfig> listVoltageTransformDetailConfig = listVoltageTransformDetailConfigCache.getIfPresent(transformId);
        if(listVoltageTransformDetailConfig == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
                SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                VoltageTransformDetailConfigMapper voltageTransformDetailConfigMapper = myBatisUtils.createMapper(VoltageTransformDetailConfigMapper.class,session);
                listVoltageTransformDetailConfig = voltageTransformDetailConfigMapper.getVoltageTransformDetailConfig(transformId);
                if(listVoltageTransformDetailConfig != null) {
                    for(VoltageTransformDetailConfig voltageTransformDetailConfig : listVoltageTransformDetailConfig){
                        voltageTransformDetailConfig.setJsonPathFieldName(Secure.decrypt(voltageTransformDetailConfig.getJsonPathFieldName()));
                    }
                    listVoltageTransformDetailConfigCache.put(transformId,listVoltageTransformDetailConfig);
                }
            }catch (Exception e){
                logger.error("Failed to load config for transformId=" + transformId, e);
            }
            logger.info("Get Data from DB");
        }else{
            logger.info("Get Data from Cache");
        }
        return listVoltageTransformDetailConfig;
    }

    public SortingConfig getSortingConfig(String transformId){
        SortingConfig sortingConfig = sortingConfigCache.getIfPresent(transformId);
        if(sortingConfig == null){
            MyBatisUtils myBatisUtils = new MyBatisUtils();
                SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
            try(SqlSession session = sqlSessionFactory.openSession(false)){
                SortingConfigMapper sortingConfigMapper = myBatisUtils.createMapper(SortingConfigMapper.class,session);
                sortingConfig = sortingConfigMapper.getSortingConfigById(transformId);
                if(sortingConfig != null) {
                    sortingConfig.setSortByFieldName(Secure.decrypt(sortingConfig.getSortByFieldName()));
                    sortingConfigCache.put(transformId,sortingConfig);
                }
            }catch (Exception e){
                logger.error("Failed to load config for transformId=" + transformId, e);
            }
            logger.info("Get Data from DB");
        }else{
            logger.info("Get Data from Cache");
        }
        return sortingConfig;
    }

}
