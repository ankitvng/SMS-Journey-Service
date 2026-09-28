package com.vonage.smsjourney.application.port.in.command;

public record SendSmsCommand(String recipient, String message, CreateSmsJourneyCommand createSmsJourneyCommand) {
    public SendSmsCommand {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient must not be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be null or blank");
        }
    }

}
