package com.volt.gen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class VoltageLibraryLoad {
    private Logger logger = LoggerFactory.getLogger(VoltageLibraryLoad.class);

    @Autowired
    CustomLibraryContextConfiguration customLibraryContextConfiguration;

    @Autowired
    CustomFpeConfiguration customFpeConfiguration;

    static {
        System.loadLibrary("vibesimplejava");
    }

    public void reload() {
        customFpeConfiguration.deleteAllCustomFpe();
        customLibraryContextConfiguration.deleteAllCustomLibrary();
        customLibraryContextConfiguration.reload();
        customFpeConfiguration.reload();
    }

    @Scheduled(cron = "0 0 0/5 * * *") // setting up to every 5 hours
    public void reloadVoltegConfig() {
        logger.info("Voltage Service - Reloading Configuration");
        reload();
    }
}
