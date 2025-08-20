package com.volt.gen.config.mybatis.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import com.volt.gen.config.mybatis.entity.VoltageCustomLibraryContextConfig;

import java.util.List;

public interface VoltageCustomLibraryContextConfigMapper {
    final String GET_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG WHERE LIBARY_CONTEXT_ID = #{libContId}";

    final String GET_ALL_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG = "SELECT * FROM VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG";

    @Select(GET_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "libraryContextId", column = "LIBRARY_CONTEXT_ID"),
            @Result(property = "policyUrl", column = "POLICY_URL")
    })
    VoltageCustomLibraryContextConfig getVoltageCustomLibraryContextConfigById(String libContId);

    @Select(GET_ALL_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG)
    @Results(value = {
            @Result(property = "libraryContextId", column = "LIBRARY_CONTEXT_ID"),
            @Result(property = "policyUrl", column = "POLICY_URL")
    })
    List<VoltageCustomLibraryContextConfig> getAllVoltageCustomLibraryContextConfig();
}
