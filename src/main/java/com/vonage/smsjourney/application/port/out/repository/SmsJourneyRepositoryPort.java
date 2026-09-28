package com.vonage.smsjourney.application.port.out.repository;

import com.vonage.smsjourney.domain.model.SmsJourney;

import java.util.List;
import java.util.Optional;

public interface SmsJourneyRepositoryPort {
    Optional<SmsJourney> findById(Long journeyId);

    List<SmsJourney> findBySmsId(Long smsId);

    SmsJourney save(SmsJourney journey);

    void delete(SmsJourney journey);

    void deleteBySmsSmsId(Long smsId);
}
