package com.volt.gen.config.mybatis.entity;

public class VoltageTransformDetailConfig {
    private String jsonTransformId;
    private String jsonPathFieldName;
    private String transformType;
    private String fpeId;

    public String getJsonTransformId() {
        return jsonTransformId;
    }

    public void setJsonTransformId(String jsonTransformId) {
        this.jsonTransformId = jsonTransformId;
    }

    public String getJsonPathFieldName() {
        return jsonPathFieldName;
    }

    public void setJsonPathFieldName(String jsonPathFieldName) {
        this.jsonPathFieldName = jsonPathFieldName;
    }

    public String getTransformType() {
        return transformType;
    }

    public void setTransformType(String transformType) {
        this.transformType = transformType;
    }

    public String getFpeId() {
        return fpeId;
    }

    public void setFpeId(String fpeId) {
        this.fpeId = fpeId;
    }

    @Override
    public String toString() {
        return "VoltageTransformDetailConfig{" +
                "jsonTransformId='" + jsonTransformId + '\'' +
                ", jsonPathFieldName='" + jsonPathFieldName + '\'' +
                ", transformType='" + transformType + '\'' +
                ", fpeId='" + fpeId + '\'' +
                '}';
    }
}
