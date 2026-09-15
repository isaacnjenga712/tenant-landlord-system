package com.apex.module.lease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing  // Enables auditing for @CreatedDate, @LastModifiedDate (optional)
public class LeaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaseServiceApplication.class, args);
    }
}
