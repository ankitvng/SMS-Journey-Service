package com.vonage.smsjourneyg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@SpringBootApplication
@EnableCaching
@EnableAsync
public class SmsJourneyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsJourneyServiceApplication.class, args);
    }

}
