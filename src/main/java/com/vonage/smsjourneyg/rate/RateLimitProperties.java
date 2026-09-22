package com.vonage.smsjourneyg.rate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "sms.rate-limit")
public class RateLimitProperties {

    @NotNull
    private int requestsLimit = 5;

    @NotNull
    private Duration timeWindow = Duration.ofSeconds(10);
}
