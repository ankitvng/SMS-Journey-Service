package com.vonage.smsjourney.application.port.out.repository;

import com.vonage.smsjourney.domain.model.Sms;

import java.util.List;
import java.util.Optional;

public interface SmsRepositoryPort {
    Optional<Sms> findActiveById(Long smsId);

    List<Sms> findAllActive();
    Sms save(Sms sms);
}
