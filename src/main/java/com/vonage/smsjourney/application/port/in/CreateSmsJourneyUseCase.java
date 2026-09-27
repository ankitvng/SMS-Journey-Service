package com.vonage.smsjourney.application.port.in;

import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.domain.model.SmsJourney;

public interface CreateSmsJourneyUseCase {
    SmsJourney createSmsJourney(Long smsId, CreateSmsJourneyCommand command);
}
