package com.vonage.smsjourneyg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "sms.rate-limit")
public class RateLimitProperties {

    private int requestsLimit = 5;

    private Duration timeWindow = Duration.ofSeconds(10);
}
