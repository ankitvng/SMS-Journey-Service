package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsRepository extends JpaRepository<Sms, Long> {
}
