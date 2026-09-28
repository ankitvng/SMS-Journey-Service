package com.vonage.smsjourney.application.service;

import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;
import com.vonage.smsjourney.application.port.in.CreateJourneyFromEventUseCase;
import com.vonage.smsjourney.application.port.in.CreateSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.application.port.out.cache.SmsJourneyCachePort;
import com.vonage.smsjourney.application.service.mapper.SmsJourneyMapper;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.vonage.smsjourney.domain.exception.JourneyNotFoundException;
import com.vonage.smsjourney.domain.exception.SmsNotFoundException;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsJourney;
import com.vonage.smsjourney.domain.model.SmsStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsJourneyApplicationService implements GetSmsJourneyUseCase, CreateSmsJourneyUseCase, DeleteSmsJourneyUseCase, CreateJourneyFromEventUseCase {

    private final SmsJourneyRepositoryPort smsJourneyRepository;
    private final SmsRepositoryPort smsRepository;
    private final SmsJourneyMapper smsJourneyMapper;
    private final SmsJourneyCachePort smsJourneyCache;

    @Override
    @Transactional
    public SmsJourney createSmsJourney(Long smsId, CreateSmsJourneyCommand command) {
        log.info("Creating SMS journey for: {}", smsId);
        Sms sms = smsRepository.findActiveById(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));

        SmsJourney smsJourney = command.createSmsJourney();
        smsJourney.setSms(sms);
        smsJourney.setStatus(SmsStatus.SCHEDULED);

        SmsJourney savedSmsJourney;
        savedSmsJourney = smsJourneyRepository.save(smsJourney);
        smsJourneyCache.invalidateAll();

        log.info("Journey {} created for SMS {}", savedSmsJourney.getId(), smsId);

        return savedSmsJourney;
    }

    @Override
    public void delete(Long smsJourneyId) {
        log.info("Deleting SMS journey with ID: {}", smsJourneyId);
        SmsJourney smsJourney = smsJourneyRepository.findById(smsJourneyId).orElseThrow(() -> new JourneyNotFoundException(smsJourneyId));
        smsJourneyRepository.delete(smsJourney);
        smsJourneyCache.invalidate(smsJourneyId);
    }

    @Override
    public List<SmsJourney> getById(Long smsId) {
        log.info("Retrieving SMS journey with SmsID: {}", smsId);
        smsRepository.findActiveById(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));
        return smsJourneyCache.getJourneysBySmsId(smsId);
    }

    @Override
    @Async("taskExecutor")
    public void createJourneyFromEvent(SmsRoutingDecisionEvent event) {

        createSmsJourney(event.smsId(), smsJourneyMapper.toCommand(event.smsJourney()));

    }
}
