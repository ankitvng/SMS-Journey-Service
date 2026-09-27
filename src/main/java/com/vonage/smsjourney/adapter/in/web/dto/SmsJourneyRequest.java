package com.vonage.smsjourney.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record SmsJourneyRequest(@NotNull String campaignName,
                                @NotNull String routingStep,
                                @NotNull String primaryRoute,
                                @NotNull String fallbackRoute,
                                @NotNull double cost) {
}
