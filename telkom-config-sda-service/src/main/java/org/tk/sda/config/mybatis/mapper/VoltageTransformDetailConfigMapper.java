package org.tk.sda.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.tk.sda.config.mybatis.entity.VoltageTransformDetailConfig;

import java.util.List;

public interface VoltageTransformDetailConfigMapper {
    final String GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID = "SELECT * FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG WHERE JSON_TRANSFORM_ID = #{jsonTransformId}";

    final String GET_ALL_VOLTAGE_TRANSFORM_DETAIL_CONFIG = "SELECT * FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG";

    final String GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_FPE_ID = "SELECT * FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG WHERE FPE_ID = #{fpeId}";

    final String GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID_AND_JSON_PATH_FIELD_NAME = "SELECT * FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG WHERE JSON_TRANSFORM_ID = #{jsonTransformId}" +
            "AND JSON_PATH_FIELD_NAME = #{jsonPathFieldName}";

    final String INSERT_VOLTAGE_TRANSFORM_DETAIL_CONFIG = "INSERT INTO VOLTAGE_TRANSFORM_DETAIL_CONFIG " +
            "VALUES(#{jsonTransformId, jdbcType=VARCHAR}, #{jsonPathFieldName, jdbcType=VARCHAR}, #{transformType, jdbcType=VARCHAR}, #{fpeId, jdbcType=VARCHAR})";

    final String UPDATE_VOLTAGE_TRANSFORM_DETAIL_CONFIG = "UPDATE VOLTAGE_TRANSFORM_DETAIL_CONFIG " +
            "SET TRANSFORM_TYPE = #{transformType}, FPE_ID = #{fpeId} " +
            "WHERE JSON_TRANSFORM_ID = #{jsonTransformId} AND JSON_PATH_FIELD_NAME = #{jsonPathFieldName}";

    final String DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID = "DELETE FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG " +
            "WHERE JSON_TRANSFORM_ID = #{jsonTransformId}";

    final String DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID_AND_NAME = "DELETE FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG " +
            "WHERE JSON_TRANSFORM_ID = #{jsonTransformId} AND JSON_PATH_FIELD_NAME = #{jsonPathFieldName}";

    final String DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_FPE_ID = "DELETE FROM VOLTAGE_TRANSFORM_DETAIL_CONFIG " +
            "WHERE FPE_ID = #{fpeId}";


    @Select(GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "jsonTransformId", column = "JSON_TRANSFORM_ID"),
            @Result(property = "jsonPathFieldName", column = "JSON_PATH_FIELD_NAME"),
            @Result(property = "transformType", column = "TRANSFORM_TYPE"),
            @Result(property = "fpeId", column = "FPE_ID")
    })
    List<VoltageTransformDetailConfig> getVoltageTransformDetailConfig(String jsonTransformId);

    @Select(GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_FPE_ID)
    @Results(value = {
            @Result(property = "jsonTransformId", column = "JSON_TRANSFORM_ID"),
            @Result(property = "jsonPathFieldName", column = "JSON_PATH_FIELD_NAME"),
            @Result(property = "transformType", column = "TRANSFORM_TYPE"),
            @Result(property = "fpeId", column = "FPE_ID")
    })
    List<VoltageTransformDetailConfig> getVoltageTransformDetailConfigByFpeId(String fpeId);

    @Select(GET_ALL_VOLTAGE_TRANSFORM_DETAIL_CONFIG)
    @Results(value = {
            @Result(property = "jsonTransformId", column = "JSON_TRANSFORM_ID"),
            @Result(property = "jsonPathFieldName", column = "JSON_PATH_FIELD_NAME"),
            @Result(property = "transformType", column = "TRANSFORM_TYPE"),
            @Result(property = "fpeId", column = "FPE_ID")
    })
    List<VoltageTransformDetailConfig> getAllVoltageTransformDetailConfig();

    @Select(GET_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID_AND_JSON_PATH_FIELD_NAME)
    @Results(value = {
            @Result(property = "jsonTransformId", column = "JSON_TRANSFORM_ID"),
            @Result(property = "jsonPathFieldName", column = "JSON_PATH_FIELD_NAME"),
            @Result(property = "transformType", column = "TRANSFORM_TYPE"),
            @Result(property = "fpeId", column = "FPE_ID")
    })
    VoltageTransformDetailConfig getVoltageTransformDetaildConfigByIdAndJsonPathName(VoltageTransformDetailConfig voltageTransformDetailConfig);

    @Insert(INSERT_VOLTAGE_TRANSFORM_DETAIL_CONFIG)
    void insertVoltageTransformDetailConfig(VoltageTransformDetailConfig voltageTransformDetailConfig);

    @Update(UPDATE_VOLTAGE_TRANSFORM_DETAIL_CONFIG)
    void updateVoltageTransformDetailConfig(VoltageTransformDetailConfig voltageTransformDetailConfig);

    @Delete(DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID)
    void deleteVoltageTransformDetailConfigById(String jsonTransformId);

    @Delete(DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_ID_AND_NAME)
    void deleteVoltageTransformDetailConfigByIdAndName(String jsonTransformId, String jsonPathFieldName);

    @Delete(DELETE_VOLTAGE_TRANSFORM_DETAIL_CONFIG_BY_FPE_ID)
    void deleteVoltageTransformDetailConfigByFpeId(String fpeId);
}
