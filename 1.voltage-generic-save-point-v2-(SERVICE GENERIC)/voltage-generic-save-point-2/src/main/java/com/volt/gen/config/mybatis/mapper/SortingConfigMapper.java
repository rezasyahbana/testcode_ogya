package com.volt.gen.config.mybatis.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import com.volt.gen.config.mybatis.entity.SortingConfig;

public interface SortingConfigMapper {
    final String GET_VOLTAGE_CONFIG_BY_ID = "SELECT * FROM SORTING_CONFIG WHERE SORTING_ID = #{id}";

    @Select(GET_VOLTAGE_CONFIG_BY_ID)
    @Results(value = {
            @Result(property = "isSort", column = "IS_SORT"),
            @Result(property = "ascdesc", column = "ASCDESC"),
            @Result(property = "sortByFieldName", column = "SORT_BY_FIELDNAME")
    })
    SortingConfig getSortingConfigById(String id);
}
