package com.volt.gen.config.mybatis.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import com.volt.gen.config.mybatis.entity.VoltageTransformDetailConfig;

import java.util.List;

public interface VoltageTransformDetailConfigMapper {
    final String GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG WHERE JSON_TRANSFORM_ID = #{jsonTransformId}";


    @Select(GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "jsonTransformId", column = "JSON_TRANSFORM_ID"),
            @Result(property = "jsonPathFieldName", column = "JSON_PATH_FIELD_NAME"),
            @Result(property = "transformType", column = "TRANSFORM_TYPE"),
            @Result(property = "fpeId", column = "FPE_ID")
    })
    List<VoltageTransformDetailConfig> getVoltageTransformDetailConfig(String jsonTransformId);

}
