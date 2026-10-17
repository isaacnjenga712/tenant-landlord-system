package com.apex.mpesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.apex.mpesa",
        "com.platform.common"
})
@EnableConfigurationProperties
@EnableScheduling
public class MpesaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MpesaApplication.class, args);
    }
}