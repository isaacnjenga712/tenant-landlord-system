package com.apex.mpesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties   // scans @ConfigurationProperties beans like DarajaConfig
@EnableScheduling               // optional – enables @Scheduled jobs if you need them later
public class MpesaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MpesaApplication.class, args);
    }
}
