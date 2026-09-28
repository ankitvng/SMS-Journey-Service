package com.vonage.smsjourney.application.service;

import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.application.kafka.SmsRoutingDecisionEvent;
import com.vonage.smsjourney.application.port.in.CreateSmsUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsUseCase;
import com.vonage.smsjourney.application.port.out.cache.SmsCachePort;
import com.vonage.smsjourney.application.port.out.cache.SmsJourneyCachePort;
import com.vonage.smsjourney.application.port.out.messaging.SmsEventPublisherPort;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.exception.SmsNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsApplicationService implements CreateSmsUseCase, GetSmsUseCase, DeleteSmsUseCase {

    private final SmsRepositoryPort smsRepository;
    private final SmsJourneyRepositoryPort journeyRepository;
    private final SmsEventPublisherPort eventPublisher;
    private final SmsCachePort smsCache;
    private final SmsJourneyCachePort journeyCache;

    @Override
    public Sms create(SendSmsCommand request) {
        Sms sms = Sms.create(request.recipient(), request.message(), Clock.systemUTC());

        log.info("Created SMS: {}", request.recipient());

        Sms savedSms = smsRepository.save(sms);
        smsCache.invalidateAllSms();
        SmsRoutingDecisionEvent event = new SmsRoutingDecisionEvent(savedSms.getId(), request.createSmsJourneyCommand().createSmsJourney());

        eventPublisher.publish(event);

        return savedSms;
    }

    @Override
    @Transactional
    public void delete(Long smsId) {
        Sms sms = smsRepository.findActiveById(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));
        log.info("Deleted SMS: {}", smsId);
        sms.delete();
        smsRepository.save(sms);
        smsCache.invalidateAllSms();
        journeyRepository.deleteBySmsSmsId(smsId);
        journeyCache.invalidateAll();

    }

    @Override
    public Sms getById(Long smsId) {
        log.info("Getting SMS by ID: {}", smsId);
        List<Sms> smsList =smsCache.getSmsBySmsId(smsId);
        return smsList.stream()
                .filter(sms -> sms.getId().equals(smsId))
                .findFirst()
                .orElseThrow(() -> new SmsNotFoundException(smsId));

    }

    @Override
    public List<Sms> getAllPaged(int page, int size) {
        log.info("Getting SMS page={} size={}", page, size);

        List<Sms> allActive = smsRepository.findAllActive();

        int start =page * size;
        if(start>allActive.size()) return List.of();

        int end = Math.min(start +size, allActive.size());
        return allActive.subList(start, end);
    }
}
