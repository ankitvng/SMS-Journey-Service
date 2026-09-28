package com.vonage.smsjourney.application.port.in.command;

import com.vonage.smsjourney.domain.model.SmsJourney;

public record CreateSmsJourneyCommand(String campaignName, String routingStep, String primaryRoute, String fallbackRoute, double cost) {
    public SmsJourney createSmsJourney() {
        return new SmsJourney(this.campaignName, this.routingStep, this.primaryRoute, this.fallbackRoute, this.cost);
    }
}
