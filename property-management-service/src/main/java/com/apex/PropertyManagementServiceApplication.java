package com.apex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.apex",
        "com.platform.common"
})
public class PropertyManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementServiceApplication.class, args);
    }
}