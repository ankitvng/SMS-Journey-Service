package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findAllByDeletedIsFalse();

    Optional<Sms> findBySmsIdAndDeletedIsFalse(Long smsId);

    boolean existsBySmsIdAndDeletedIsFalse(Long smsId);

}
