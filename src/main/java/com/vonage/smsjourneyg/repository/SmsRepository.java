package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findAllByDeletedIsFalse();

    Optional<Sms> findBySmsIdAndDeletedIsFalse(Long smsId);

    boolean existsBySmsIdAndDeletedIsFalse(Long smsId);

    Optional<Sms> findByExternalSmsId(String smsId);
}
