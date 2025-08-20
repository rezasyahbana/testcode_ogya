package com.volt.gen.controller;

import com.volt.gen.service.Impl.VoltageFpeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.gen.service.KafkaProducerService;

import com.volt.gen.util.LogObjectWrapper;

@RestController
@RequestMapping(value = "/voltage")
@CrossOrigin(origins = "*")
public class VoltageFpeController {

        @Autowired
        VoltageFpeServiceImpl voltageFpeServiceImpl;

        @Autowired
        private KafkaProducerService kafkaProducerService;

        @PostMapping(path = "/transform/fpe", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> transformData(@RequestHeader String transformId,
                        @RequestBody String body) {
                String resultJson = voltageFpeServiceImpl.transformData(transformId, body);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resultJson);
        }

        @PostMapping(path = "/check-service")
        public ResponseEntity<String> checkService(@RequestBody String body) {
                String resultJson = voltageFpeServiceImpl.checkService(body);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resultJson);
        }

        @GetMapping("/coba")
        public String coba(@RequestParam(required = false, defaultValue = "default") String msg) {
                var low = LogObjectWrapper.builder().message(msg).build();
                kafkaProducerService.send(low.toLogObject());
                return msg;
        }
}
