package org.tk.sda.config.mybatis.entity;

public class VoltageCustomFpeConfig {
    private String fpeId;
    private String libraryContextId;
    private String identity;
    private String sharedSecret;
    private String format;

    public String getFpeId() {
        return fpeId;
    }

    public void setFpeId(String fpeId) {
        this.fpeId = fpeId;
    }

    public String getLibraryContextId() {
        return libraryContextId;
    }

    public void setLibraryContextId(String libraryContextId) {
        this.libraryContextId = libraryContextId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "VoltageCustomFpeConfig{" +
                "fpeId='" + fpeId + '\'' +
                ", libraryContextId='" + libraryContextId + '\'' +
                ", identity='" + identity + '\'' +
                ", sharedSecret='" + sharedSecret + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
