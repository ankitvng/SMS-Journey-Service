package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.SmsJourney;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsJourneyRepository extends JpaRepository<SmsJourney, Long> {

    List<SmsJourney> findBySmsSmsId(Long smsId);
}