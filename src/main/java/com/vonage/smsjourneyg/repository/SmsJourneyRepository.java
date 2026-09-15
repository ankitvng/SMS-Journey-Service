package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.SmsJourney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsJourneyRepository extends JpaRepository<SmsJourney, Long> {

    List<SmsJourney> findBySmsSmsId(Long smsId);

    void deleteBySmsSmsId(Long smsId);
}
