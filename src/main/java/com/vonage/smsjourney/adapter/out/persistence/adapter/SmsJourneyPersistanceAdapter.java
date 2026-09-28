package com.vonage.smsjourney.adapter.out.persistence.adapter;

import com.vonage.smsjourney.adapter.out.persistence.repository.SpringDataSmsJourneyRepository;
import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJourneyJpaEntity;
import com.vonage.smsjourney.adapter.out.persistence.mapper.SmsJourneyPersistanceMapper;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.domain.exception.JourneyNotFoundException;
import com.vonage.smsjourney.domain.model.SmsJourney;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Data
@Repository
public class SmsJourneyPersistanceAdapter implements SmsJourneyRepositoryPort {

    private final SpringDataSmsJourneyRepository smsJourneyRepository;
    private final SmsJourneyPersistanceMapper smsJourneyPersistanceMapper;

    @Override
    public Optional<SmsJourney> findById(Long journeyId) {
        return Optional.of(smsJourneyRepository.findById(journeyId).map(smsJourneyPersistanceMapper::toDomain).orElseThrow(() -> new JourneyNotFoundException(journeyId)));
    }

    @Override
    public List<SmsJourney> findBySmsId(Long smsId) {
        return smsJourneyRepository.findBySmsSmsId(smsId).stream().map(smsJourneyPersistanceMapper::toDomain).toList();
    }

    @Override
    public SmsJourney save(SmsJourney journey) {
        SmsJourneyJpaEntity journeyEntity = smsJourneyPersistanceMapper.toJpaEntity(journey);
        SmsJourneyJpaEntity savedEntity = smsJourneyRepository.save(journeyEntity);
        return smsJourneyPersistanceMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(SmsJourney journey) {
        smsJourneyRepository.delete(smsJourneyPersistanceMapper.toJpaEntity(journey));
    }

    @Override
    public void deleteBySmsSmsId(Long smsId) {
        smsJourneyRepository.deleteBySmsSmsId(smsId);
    }
}
