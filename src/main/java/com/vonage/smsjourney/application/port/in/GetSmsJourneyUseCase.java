package com.vonage.smsjourney.application.port.in;

import com.vonage.smsjourney.domain.model.SmsJourney;

import java.util.List;

public interface GetSmsJourneyUseCase {
    List<SmsJourney> getById(Long smsId);
}
