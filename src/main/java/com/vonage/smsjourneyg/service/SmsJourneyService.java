package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.cache.SmsJourneyCache;
import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.dto.SmsRoutingDecisionEvent;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.exception.JourneyNotFoundException;
import com.vonage.smsjourneyg.exception.SmsNotFoundException;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import com.vonage.smsjourneyg.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsJourneyService {

    private final SmsJourneyRepository journeyRepository;
    private final SmsRepository smsRepository;
    private final SmsJourneyCache smsJourneyCache;


    public SmsJourneyDto createJourney(Long smsId, SmsJourneyDto request) {

        log.info("Creating journey for SMS {}", smsId);

        Sms sms = smsRepository.findBySmsIdAndDeletedIsFalse(smsId).orElseThrow(() -> new SmsNotFoundException(smsId));

        SmsJourney journey = new SmsJourney();
        journey.setSms(sms);
        journey.setCampaignName(request.getCampaignName());
        journey.setRoutingStep(request.getRoutingStep());
        journey.setPrimaryRoute(request.getPrimaryRoute());
        journey.setFallbackRoute(request.getFallbackRoute());
        journey.setCost(request.getCost());
        journey.setScheduledTime(request.getScheduledTime());
        journey.setStatus(SmsStatus.SCHEDULED);

        SmsJourney savedJourney = journeyRepository.save(journey);

        log.info("Journey {} created for SMS {}", savedJourney.getId(), smsId);

        smsJourneyCache.invalidate(smsId);

        return convertToDto(savedJourney);
    }

    @Async("taskExecutor")
    @Transactional
    public void createJourneyfromEvent(SmsRoutingDecisionEvent event) {

        createJourney(event.getSmsId(), event.getSmsJourneyDto());

    }


    public List<SmsJourneyDto> getJourneysBySms(Long smsId) {

        log.info("Fetching journeys for SMS {}", smsId);

        return smsJourneyCache.getJourneysBySmsId(smsId);
    }


    public void deleteJourney(Long journeyId) {

        log.info("Deleting journey {}", journeyId);

        SmsJourney journey = journeyRepository.findById(journeyId).orElseThrow(() -> new JourneyNotFoundException(journeyId));

        Long smsId = journey.getSms().getSmsId();

        journeyRepository.delete(journey);

        log.info("Journey {} deleted successfully", journeyId);

        smsJourneyCache.invalidate(smsId);
    }


    private SmsJourneyDto convertToDto(SmsJourney journey) {

        SmsJourneyDto dto = new SmsJourneyDto();

        dto.setId(journey.getId());
        dto.setCampaignName(journey.getCampaignName());
        dto.setRoutingStep(journey.getRoutingStep());
        dto.setPrimaryRoute(journey.getPrimaryRoute());
        dto.setFallbackRoute(journey.getFallbackRoute());
        dto.setCost(journey.getCost());
        dto.setStatus(journey.getStatus());
        dto.setScheduledTime(journey.getScheduledTime());

        return dto;
    }
}
