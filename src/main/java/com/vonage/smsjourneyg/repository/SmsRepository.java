package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findAllByDeletedAtIsNull();

    Optional<Sms> findBySmsIdAndDeletedAtIsNull(Long smsId);

    boolean existsBySmsIdAndDeletedAtIsNull(Long smsId);
}
