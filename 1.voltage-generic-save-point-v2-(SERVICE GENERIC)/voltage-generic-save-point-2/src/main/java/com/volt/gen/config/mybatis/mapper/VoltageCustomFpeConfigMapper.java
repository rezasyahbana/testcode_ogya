package com.volt.gen.config.mybatis.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import com.volt.gen.config.mybatis.entity.VoltageCustomFpeConfig;

import java.util.List;

public interface VoltageCustomFpeConfigMapper {
    final String GET_VOLTAGE_CUSTOM_FPE_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_CUSTOM_FPE_CONFIG WHERE FPE_ID = #{fpeId}";

    final String GET_ALL_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG = "SELECT * FROM VOLTAGE_CUSTOM_FPE_CONFIG";

    @Select(GET_VOLTAGE_CUSTOM_FPE_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "fpeId", column = "FPE_ID"),
            @Result(property = "libraryContextId", column = "LIBRARY_CONTEXT_ID"),
            @Result(property = "identity", column = "IDENTITY"),
            @Result(property = "sharedSecret", column = "SHARED_SECRET"),
            @Result(property = "format", column = "FORMAT")
    })
    VoltageCustomFpeConfig getVoltageCustomFpeConfigById(String fpeId);

    @Select(GET_ALL_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG)
    @Results(value = {
            @Result(property = "fpeId", column = "FPE_ID"),
            @Result(property = "libraryContextId", column = "LIBRARY_CONTEXT_ID"),
            @Result(property = "identity", column = "IDENTITY"),
            @Result(property = "sharedSecret", column = "SHARED_SECRET"),
            @Result(property = "format", column = "FORMAT")
    })
    List<VoltageCustomFpeConfig> getAllVoltageCustomFpeConfig();
}
