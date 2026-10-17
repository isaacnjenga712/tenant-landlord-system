package com.apex.maintenanceTicketService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.apex.maintenanceTicketService",
        "com.platform.common"
})
public class MaintenanceTicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaintenanceTicketServiceApplication.class, args);
    }
}
