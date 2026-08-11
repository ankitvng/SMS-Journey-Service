package com.vonage.smsjourneyg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@SpringBootApplication
public class SmsJourneyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsJourneyServiceApplication.class, args);
    }

}
