package com.volt.gen.service;

import com.telkom.logging.avro.schema.LogObject;

public interface KafkaProducerService {
    void send(LogObject logObject);
    void sendRaw(String rawMessage);
}
