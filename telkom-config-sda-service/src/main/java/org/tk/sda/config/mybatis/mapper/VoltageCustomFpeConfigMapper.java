package org.tk.sda.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.tk.sda.config.mybatis.entity.VoltageCustomFpeConfig;

import java.util.List;

public interface VoltageCustomFpeConfigMapper {
    final String GET_VOLTAGE_CUSTOM_FPE_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_CUSTOM_FPE_CONFIG WHERE FPE_ID = #{fpeId}";

    final String GET_VOLTAGE_CUSTOM_FPE_CONFIG_BY_LIBRARY_CONTEXT_ID = "SELECT * FROM VOLTAGE_CUSTOM_FPE_CONFIG WHERE FPE_ID = #{fpeId}";

    final String GET_ALL_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG = "SELECT * FROM VOLTAGE_CUSTOM_FPE_CONFIG";

    final String INSERT_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG = "INSERT INTO VOLTAGE_CUSTOM_FPE_CONFIG " +
            "VALUES (#{fpeId, jdbcType=VARCHAR}, #{libraryContextId, jdbcType=VARCHAR}, #{identity, jdbcType=VARCHAR}" +
            ", #{sharedSecret, jdbcType=VARCHAR}, #{format, jdbcType=VARCHAR})";

    final String UPDATE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG = "UPDATE VOLTAGE_CUSTOM_FPE_CONFIG" +
            " SET LIBRARY_CONTEXT_ID = #{libraryContextId}, IDENTITY = #{identity}, SHARED_SECRET = #{sharedSecret}" +
            ", FORMAT = #{format} WHERE FPE_ID = #{fpeId}";

    final String DELETE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG = "DELETE FROM VOLTAGE_CUSTOM_FPE_CONFIG " +
            "WHERE FPE_ID = #{fpeId}";

    final String DELETE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG_BY_LIBRARY_CONTEXT_ID = "DELETE FROM VOLTAGE_CUSTOM_FPE_CONFIG" +
            " WHERE LIBRARY_CONTEXT_ID = #{libraryContextId}";

    @Select(GET_VOLTAGE_CUSTOM_FPE_CONFIG_BY_LIBRARY_CONTEXT_ID)
    @Results(value = {
            @Result(property = "fpeId", column = "FPE_ID"),
            @Result(property = "libraryContextId", column = "LIBRARY_CONTEXT_ID"),
            @Result(property = "identity", column = "IDENTITY"),
            @Result(property = "sharedSecret", column = "SHARED_SECRET"),
            @Result(property = "format", column = "FORMAT")
    })
    List<VoltageCustomFpeConfig> getVoltageCustomFpeConfigByLibraryContextId(String libraryContextId);

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

    @Insert(INSERT_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG)
    void insertVoltageCustomFpeLoadConfig(VoltageCustomFpeConfig voltageCustomFpeConfig);

    @Update(UPDATE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG)
    void updateVoltageCustomFpeLoadConfig(VoltageCustomFpeConfig voltageCustomFpeConfig);

    @Delete(DELETE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG)
    void deleteVoltageCustomFpeLoadConfig(String fpeId);

    @Delete(DELETE_VOLTAGE_CUSTOM_FPE_LOAD_CONFIG_BY_LIBRARY_CONTEXT_ID)
    void deleteVoltageCustomFpeLoadConfigByLibraryContextId(String libraryContextId);
}
