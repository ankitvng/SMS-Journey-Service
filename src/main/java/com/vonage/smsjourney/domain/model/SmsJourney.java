package com.vonage.smsjourney.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SmsJourney {

    private Long id;
    private String campaignName;
    private String routingStep;
    private String primaryRoute;
    private String fallbackRoute;
    private double cost;
    private SmsStatus status ;
    private LocalDateTime scheduledTime;
    private Sms sms;

    public SmsJourney(String campaignName, String routingStep, String primaryRoute, String fallbackRoute, double cost) {
        this.campaignName = campaignName;
        this.routingStep = routingStep;
        this.primaryRoute = primaryRoute;
        this.fallbackRoute = fallbackRoute;
        this.cost = cost;
    }


}
