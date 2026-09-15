package com.apex.leaseService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.apex.leaseService.entity")
@EnableJpaRepositories(basePackages = "com.apex.leaseService.repository")
public class LeaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeaseServiceApplication.class, args);
    }
}
