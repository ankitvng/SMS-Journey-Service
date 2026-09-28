package com.vonage.smsjourney.application.port.out.cache;

import com.vonage.smsjourney.domain.model.SmsJourney;

import java.util.List;

public interface SmsJourneyCachePort {
    List<SmsJourney> getJourneysBySmsId(Long smsId);
    void invalidate(Long smsId);
    void invalidateAll();
}
