package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import com.vonage.smsjourneyg.repository.SmsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsJourneyServiceTest {

    @Mock
    private SmsJourneyRepository journeyRepository;

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private SmsRoutingService routingService;

    @InjectMocks
    private SmsJourneyService journeyService;

    private Sms sms;
    private SmsJourney journey;
    private SmsJourneyDto requestDto;

    @BeforeEach
    void setUp() {
        sms = new Sms();
        sms.setSmsId(100L);
        sms.setRecipient("+919876543210");
        sms.setMessage("Test Message");
        sms.setStatus(SmsStatus.CREATED);

        journey = new SmsJourney();
        journey.setId(1L);
        journey.setSms(sms);
        journey.setCampaignName("Summer Promo");
        journey.setRoutingStep("PRIMARY_ATTEMPT");
        journey.setPrimaryRoute("ROUTE_A");
        journey.setFallbackRoute("ROUTE_B");
        journey.setCost(0.05);
        journey.setStatus(SmsStatus.SCHEDULED);
        journey.setScheduledTime(LocalDateTime.of(2026, 8, 17, 12, 0));

        requestDto = new SmsJourneyDto();
        requestDto.setCampaignName("Summer Promo");
        requestDto.setRoutingStep("PRIMARY_ATTEMPT");
        requestDto.setPrimaryRoute("ROUTE_A");
        requestDto.setFallbackRoute("ROUTE_B");
        requestDto.setCost(0.05);
        requestDto.setScheduledTime(LocalDateTime.of(2026, 8, 17, 12, 0));
    }

    // --- createJourney Tests ---

    @Test
    void createJourney_WhenSmsExists_ShouldCreateAndReturnDto() {
        when(smsRepository.findById(100L)).thenReturn(Optional.of(sms));
        when(journeyRepository.save(any(SmsJourney.class))).thenReturn(journey);

        SmsJourneyDto result = journeyService.createJourney(100L, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Summer Promo", result.getCampaignName());
        assertEquals("PRIMARY_ATTEMPT", result.getRoutingStep());
        assertEquals("ROUTE_A", result.getPrimaryRoute());
        assertEquals("ROUTE_B", result.getFallbackRoute());
        assertEquals(0.05, result.getCost());
        assertEquals(SmsStatus.SCHEDULED, result.getStatus());
        assertEquals(LocalDateTime.of(2026, 8, 17, 12, 0), result.getScheduledTime());

        verify(smsRepository, times(1)).findById(100L);
        verify(journeyRepository, times(1)).save(any(SmsJourney.class));
    }

    @Test
    void createJourney_WhenSmsDoesNotExist_ShouldThrowRuntimeException() {
        when(smsRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> journeyService.createJourney(100L, requestDto)
        );

        assertEquals("SMS not found: 100", exception.getMessage());
        verify(smsRepository, times(1)).findById(100L);
        verify(journeyRepository, never()).save(any());
    }

    // --- getJourneysBySms Tests ---

    @Test
    void getJourneysBySms_WhenSmsExists_ShouldReturnJourneyList() {
        when(smsRepository.existsById(100L)).thenReturn(true);
        when(journeyRepository.findBySmsSmsId(100L)).thenReturn(List.of(journey));

        List<SmsJourneyDto> result = journeyService.getJourneysBySms(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Summer Promo", result.get(0).getCampaignName());

        verify(smsRepository, times(1)).existsById(100L);
        verify(journeyRepository, times(1)).findBySmsSmsId(100L);
    }

    @Test
    void getJourneysBySms_WhenSmsDoesNotExist_ShouldThrowRuntimeException() {
        when(smsRepository.existsById(100L)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> journeyService.getJourneysBySms(100L)
        );

        assertEquals("SMS not found: 100", exception.getMessage());
        verify(smsRepository, times(1)).existsById(100L);
        verify(journeyRepository, never()).findBySmsSmsId(anyLong());
    }

    // --- processJourney Tests ---

    @Test
    void processJourney_PrimaryRouteSuccess_ShouldSetStatusSent() {
        when(journeyRepository.findById(1L)).thenReturn(Optional.of(journey));
        when(routingService.sendSms("ROUTE_A", sms)).thenReturn(true);

        journeyService.processJourney(1L);

        assertEquals(SmsStatus.SENT, journey.getStatus());
        assertEquals(SmsStatus.SENT, sms.getStatus());

        verify(routingService, times(1)).sendSms("ROUTE_A", sms);
        verify(routingService, never()).sendSms(eq("ROUTE_B"), any());
        verify(journeyRepository, times(1)).save(journey);
        verify(smsRepository, times(1)).save(sms);
    }

    @Test
    void processJourney_PrimaryFails_FallbackSuccess_ShouldSetStatusSent() {
        when(journeyRepository.findById(1L)).thenReturn(Optional.of(journey));
        when(routingService.sendSms("ROUTE_A", sms)).thenReturn(false);
        when(routingService.sendSms("ROUTE_B", sms)).thenReturn(true);

        journeyService.processJourney(1L);

        assertEquals("SENT", journey.getStatus());
        assertEquals("SENT", sms.getStatus());

        verify(routingService, times(1)).sendSms("ROUTE_A", sms);
        verify(routingService, times(1)).sendSms("ROUTE_B", sms);
        verify(journeyRepository, times(1)).save(journey);
        verify(smsRepository, times(1)).save(sms);
    }

    @Test
    void processJourney_BothRoutesFail_ShouldSetStatusFailed() {
        when(journeyRepository.findById(1L)).thenReturn(Optional.of(journey));
        when(routingService.sendSms("ROUTE_A", sms)).thenReturn(false);
        when(routingService.sendSms("ROUTE_B", sms)).thenReturn(false);

        journeyService.processJourney(1L);

        assertEquals(SmsStatus.FAILED, journey.getStatus());
        assertEquals(SmsStatus.FAILED, sms.getStatus());

        verify(routingService, times(1)).sendSms("ROUTE_A", sms);
        verify(routingService, times(1)).sendSms("ROUTE_B", sms);
        verify(journeyRepository, times(1)).save(journey);
        verify(smsRepository, times(1)).save(sms);
    }

    @Test
    void processJourney_WhenJourneyDoesNotExist_ShouldThrowRuntimeException() {
        when(journeyRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> journeyService.processJourney(1L)
        );

        assertEquals("Journey not found: 1", exception.getMessage());
        verify(journeyRepository, times(1)).findById(1L);
        verify(routingService, never()).sendSms(any(), any());
        verify(journeyRepository, never()).save(any());
        verify(smsRepository, never()).save(any());
    }
}