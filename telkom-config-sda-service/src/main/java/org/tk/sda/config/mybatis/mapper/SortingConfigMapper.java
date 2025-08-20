package org.tk.sda.config.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import org.tk.sda.config.mybatis.entity.SortingConfig;

import java.util.List;

public interface SortingConfigMapper {
    final String GET_SORTING_CONFIG_BY_ID = "SELECT * FROM SORTING_CONFIG WHERE SORTING_ID = #{sortingId}";

    final String GET_ALL_SORTING_CONFIG = "SELECT * FROM SORTING_CONFIG";
// (SORTING_ID, IS_ARRAY, IS_SORT, ASCDESC, SORT_BY_FIELDNAME)
    final String INSERT_SORTING_CONFIG = "INSERT INTO SORTING_CONFIG " +
            "VALUES(#{sortingId, jdbcType=VARCHAR}, #{isArray, jdbcType=VARCHAR}, #{isSort, jdbcType=BOOLEAN}" +
            ", #{ascdesc, jdbcType=VARCHAR}, #{sortByFieldName, jdbcType=VARCHAR})";

    final String UPDATE_SORTING_CONFIG = "UPDATE SORTING_CONFIG SET IS_SORT = #{isSort}, ASCDESC = #{ascdesc}, SORT_BY_FIELDNAME = #{sortByFieldName} " +
            "WHERE SORTING_ID = #{sortingId}";

    final String DELETE_SORTING_CONFIG = "DELETE FROM SORTING_CONFIG WHERE SORTING_ID = #{sortingId}";

    @Select(GET_SORTING_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "sortingId", column = "SORTING_ID"),
            @Result(property = "isArray", column = "IS_ARRAY"),
            @Result(property = "isSort", column = "IS_SORT"),
            @Result(property = "ascdesc", column = "ASCDESC"),
            @Result(property = "sortByFieldName", column = "SORT_BY_FIELDNAME")
    })
    SortingConfig getSortingConfigById(String sortingId);

    @Select(GET_ALL_SORTING_CONFIG)
    @Results(value = {
            @Result(property = "sortingId", column = "SORTING_ID"),
            @Result(property = "isArray", column = "IS_ARRAY"),
            @Result(property = "isSort", column = "IS_SORT"),
            @Result(property = "ascdesc", column = "ASCDESC"),
            @Result(property = "sortByFieldName", column = "SORT_BY_FIELDNAME")
    })
    List<SortingConfig> getAllSortingConfig();

    @Insert(INSERT_SORTING_CONFIG)
    void insertSortingConfig(SortingConfig sortingConfig);

    @Update(UPDATE_SORTING_CONFIG)
    void updateSortingConfig(SortingConfig sortingConfig);

    @Delete(DELETE_SORTING_CONFIG)
    void deleteSortingConfigById(String sortingId);
}
