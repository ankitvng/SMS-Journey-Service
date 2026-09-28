package com.vonage.smsjourney.application.service.mapper;

import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.domain.model.SmsJourney;
import org.springframework.stereotype.Component;

@Component
public class SmsJourneyMapper {
    public CreateSmsJourneyCommand toCommand(SmsJourney smsJourney) {
        return new CreateSmsJourneyCommand(
                smsJourney.getCampaignName(),
                smsJourney.getRoutingStep(),
                smsJourney.getPrimaryRoute(),
                smsJourney.getFallbackRoute(),
                smsJourney.getCost()
        );
    }
}
