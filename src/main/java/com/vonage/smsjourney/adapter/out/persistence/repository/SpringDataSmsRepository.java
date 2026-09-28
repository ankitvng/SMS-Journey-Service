package com.vonage.smsjourney.adapter.out.persistence.repository;

import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSmsRepository extends JpaRepository<SmsJpaEntity, Long> {

    Optional<SmsJpaEntity> findBySmsIdAndDeletedIsFalse(Long smsId);

    List<SmsJpaEntity> findAllByDeletedIsFalse();
}
