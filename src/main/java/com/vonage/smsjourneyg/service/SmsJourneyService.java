package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import com.vonage.smsjourneyg.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsJourneyService {

    private final SmsJourneyRepository journeyRepository;
    private final SmsRepository smsRepository;
    private final SmsRoutingService routingService;


    // Create a journey for an SMS
    public SmsJourneyDto createJourney(
            Long smsId,
            SmsJourneyDto request) {

        log.info(
                "Creating journey for SMS {}",
                smsId
        );

        Sms sms = smsRepository.findById(smsId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "SMS not found: " + smsId
                        )
                );

        SmsJourney journey = new SmsJourney();

        journey.setSms(sms);
        journey.setCampaignName(
                request.getCampaignName()
        );
        journey.setRoutingStep(
                request.getRoutingStep()
        );
        journey.setPrimaryRoute(
                request.getPrimaryRoute()
        );
        journey.setFallbackRoute(
                request.getFallbackRoute()
        );
        journey.setCost(
                request.getCost()
        );
        journey.setScheduledTime(
                request.getScheduledTime()
        );
        journey.setStatus("SCHEDULED");

        SmsJourney savedJourney =
                journeyRepository.save(journey);

        log.info(
                "Journey {} created for SMS {}",
                savedJourney.getId(),
                smsId
        );

        return convertToDto(savedJourney);
    }


    // Get all journeys belonging to an SMS
    public List<SmsJourneyDto> getJourneysBySms(
            Long smsId) {

        log.info(
                "Fetching journeys for SMS {}",
                smsId
        );

        if (!smsRepository.existsById(smsId)) {
            throw new RuntimeException(
                    "SMS not found: " + smsId
            );
        }

        return journeyRepository
                .findBySmsSmsId(smsId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // Process a journey
    public void processJourney(Long journeyId) {

        log.info(
                "Processing journey {}",
                journeyId
        );

        SmsJourney journey =
                journeyRepository.findById(journeyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Journey not found: "
                                                + journeyId
                                )
                        );

        Sms sms = journey.getSms();

        // Try primary route
        boolean primarySuccess =
                routingService.sendSms(
                        journey.getPrimaryRoute(),
                        sms
                );

        if (primarySuccess) {

            journey.setStatus("SENT");

            sms.setStatus("SENT");

            log.info(
                    "SMS {} successfully sent through primary route {}",
                    sms.getSmsId(),
                    journey.getPrimaryRoute()
            );

        } else {

            log.warn(
                    "Primary route failed for SMS {}, trying fallback",
                    sms.getSmsId()
            );

            // Try fallback
            boolean fallbackSuccess =
                    routingService.sendSms(
                            journey.getFallbackRoute(),
                            sms
                    );

            if (fallbackSuccess) {

                journey.setStatus("SENT");

                sms.setStatus("SENT");

                log.info(
                        "SMS {} successfully sent through fallback route {}",
                        sms.getSmsId(),
                        journey.getFallbackRoute()
                );

            } else {

                journey.setStatus("FAILED");

                sms.setStatus("FAILED");

                log.error(
                        "SMS {} failed through both routes",
                        sms.getSmsId()
                );
            }
        }

        journeyRepository.save(journey);
        smsRepository.save(sms);
    }


    private SmsJourneyDto convertToDto(
            SmsJourney journey) {

        SmsJourneyDto dto = new SmsJourneyDto();

        dto.setId(journey.getId());
        dto.setCampaignName(
                journey.getCampaignName()
        );
        dto.setRoutingStep(
                journey.getRoutingStep()
        );
        dto.setPrimaryRoute(
                journey.getPrimaryRoute()
        );
        dto.setFallbackRoute(
                journey.getFallbackRoute()
        );
        dto.setCost(
                journey.getCost()
        );
        dto.setStatus(
                journey.getStatus()
        );
        dto.setScheduledTime(
                journey.getScheduledTime()
        );

        return dto;
    }
}