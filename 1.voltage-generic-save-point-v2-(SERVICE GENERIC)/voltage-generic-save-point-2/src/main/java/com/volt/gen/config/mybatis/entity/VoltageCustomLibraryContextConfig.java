package com.volt.gen.config.mybatis.entity;

public class VoltageCustomLibraryContextConfig {
    private String libraryContextId;
    private String policyUrl;


    public String getLibraryContextId() {
        return libraryContextId;
    }

    public void setLibraryContextId(String libraryContextId) {
        this.libraryContextId = libraryContextId;
    }

    public String getPolicyUrl() {
        return policyUrl;
    }

    public void setPolicyUrl(String policyUrl) {
        this.policyUrl = policyUrl;
    }

    @Override
    public String toString() {
        return "VoltageCustomLibraryContextConfig{" +
                "libraryContextId='" + libraryContextId + '\'' +
                ", policyUrl='" + policyUrl + '\'' +
                '}';
    }
}
