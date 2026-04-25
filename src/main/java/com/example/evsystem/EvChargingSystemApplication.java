package com.example.evsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvChargingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvChargingSystemApplication.class, args);
    }

}
