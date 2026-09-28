package com.vonage.smsjourney.adapter.in.web.mapper;

import com.vonage.smsjourney.adapter.in.web.dto.SmsJourneyRequest;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import org.springframework.stereotype.Component;

@Component
public class SmsJourneyWebMapper {
    public static CreateSmsJourneyCommand toCreateSmsJourneyCommand(SmsJourneyRequest request) {
        String campaignName = request.campaignName();
        String routingStep = request.routingStep();
        String primaryRoute = request.primaryRoute();
        String fallbackRoute = request.fallbackRoute();
        double cost = request.cost();
        return new CreateSmsJourneyCommand(campaignName, routingStep, primaryRoute, fallbackRoute, cost);
    }
}
