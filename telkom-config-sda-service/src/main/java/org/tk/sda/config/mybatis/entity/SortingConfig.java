package org.tk.sda.config.mybatis.entity;

public class SortingConfig {
    private String sortingId;
    private String isArray;
    private boolean isSort;
    private String ascdesc;
    private String sortByFieldName;

    public String getId() {
        return sortingId;
    }

    public void setId(String id) {
        this.sortingId = id;
    }

    public String getIsArray() {
        return isArray;
    }

    public void setIsArray(String isArray) {
        this.isArray = isArray;
    }

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
                "sortingId='" + sortingId + '\'' +
                ", isArray='" + isArray + '\'' +
                ", isSort=" + isSort +
                ", ascdesc='" + ascdesc + '\'' +
                ", sortByFieldName='" + sortByFieldName + '\'' +
                '}';
    }
}
