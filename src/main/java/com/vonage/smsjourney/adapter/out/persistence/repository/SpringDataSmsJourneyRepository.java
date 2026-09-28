package com.vonage.smsjourney.adapter.out.persistence.repository;

import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJourneyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataSmsJourneyRepository extends JpaRepository<SmsJourneyJpaEntity, Long> {
    List<SmsJourneyJpaEntity> findBySmsSmsId(Long smsId);

    void deleteBySmsSmsId(Long smsId);
}
