package com.volt.gen.config.mybatis.entity;

public class SortingConfig {
    private boolean isSort;
    private String ascdesc;
    private String sortByFieldName;

    public boolean getIsSort() {
        return isSort;
    }

    public void setIsSort(boolean isSort) {
        this.isSort = isSort;
    }

    public String getAscdesc() {
        return ascdesc;
    }

    public void setAscdesc(String ascdesc) {
        this.ascdesc = ascdesc;
    }

    public String getSortByFieldName() {
        return sortByFieldName;
    }

    public void setSortByFieldName(String sortByFieldName) {
        this.sortByFieldName = sortByFieldName;
    }

    @Override
    public String toString() {
        return "SortingConfig{" +
                "isSort='" + isSort + '\'' +
                ", ascdesc='" + ascdesc + '\'' +
                ", sortByFieldName='" + sortByFieldName + '\'' +
                '}';
    }
}
