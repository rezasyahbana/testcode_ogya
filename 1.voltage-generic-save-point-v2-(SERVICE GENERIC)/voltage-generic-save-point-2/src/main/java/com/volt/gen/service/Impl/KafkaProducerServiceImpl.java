package com.volt.gen.service.Impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.telkom.logging.avro.schema.LogObject;
import com.volt.gen.service.KafkaProducerService;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {
    @Value("${config.kafka.topic-name}")
    private String topicName;

    @Value("${config.kafka.graylog-topic-name}")
    private String graylogTopicName;

    private Logger logger = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    private final KafkaTemplate<String, LogObject> template;
    private final KafkaTemplate<String, String> grayplate;

    KafkaProducerServiceImpl(KafkaTemplate<String, LogObject> template,KafkaTemplate<String, String> grayplate) {
        this.template = template;
        this.grayplate = grayplate;
    }

    public void send(LogObject logObject) {
        try {
            template.send(topicName, UUID.randomUUID().toString(), logObject).get();
            logger.info("message sent successfully!");
        } catch (Exception e) {
            logger.error("failed to send message: ", e);
        }
    }
    public void sendRaw(String rawMessage) {
        try {
            grayplate.send(graylogTopicName, UUID.randomUUID().toString(), rawMessage).get();
            logger.info("Raw message sent successfully!");
        } catch (Exception e) {
            logger.error("Failed to send raw message: ", e);
        }
    }

//    public void send(LogObject logObject) {
//        var sendResult = template.send(topicName, UUID.randomUUID().toString(), logObject);
//        sendResult.addCallback(result -> logger.info("message sent successfully!"),
//                failure -> logger.error("failed to send message: ", failure));
//    }

}
