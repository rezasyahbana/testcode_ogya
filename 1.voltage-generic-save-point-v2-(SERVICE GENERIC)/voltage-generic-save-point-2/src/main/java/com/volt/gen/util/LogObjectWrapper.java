package com.volt.gen.util;

import com.telkom.logging.avro.schema.LogObject;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class LogObjectWrapper {
    @Builder.Default
    private String message = null;
    @Builder.Default
    private String transformId = "";
    @Builder.Default
    private String type = "ENCRYPT";
    @Builder.Default
    private String status = "success";
    @Builder.Default
    private String hostname = "";
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    public LogObject toLogObject() {
        return new LogObject(timestamp, hostname, transformId, type, message, status);
    }
}
