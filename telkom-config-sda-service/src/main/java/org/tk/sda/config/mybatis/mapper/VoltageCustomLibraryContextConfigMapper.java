package org.tk.sda.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.tk.sda.config.mybatis.entity.VoltageCustomLibraryContextConfig;

import java.util.List;

public interface VoltageCustomLibraryContextConfigMapper {
    final String GET_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG WHERE LIBRARY_CONTEXT_ID = #{libContId}";

    final String GET_ALL_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG = "SELECT * FROM VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG";

    final String INSERT_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG = "INSERT INTO VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG" +
            " VALUES (#{libraryContextId, jdbcType=VARCHAR}, #{policyUrl, jdbcType=VARCHAR})";

    final String UPDATE_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG = "UPDATE VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG " +
            "SET POLICY_URL = #{policyUrl} WHERE LIBRARY_CONTEXT_ID = #{libraryContextId}";

    final String DELETE_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG = "DELETE FROM VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG " +
            "WHERE LIBRARY_CONTEXT_ID = #{libraryContextId}";

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

    @Insert(INSERT_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG)
    void insertVoltageCustomLibraryContextConfig(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig);

    @Update(UPDATE_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG)
    void updateVoltageCustomLibraryContextConfig(VoltageCustomLibraryContextConfig voltageCustomLibraryContextConfig);

    @Delete(DELETE_VOLTAGE_CUSTOM_LIBRARY_CONTEXT_CONFIG)
    void deleteVoltageCustomLibraryContextConfig(String libraryContextId);
}
