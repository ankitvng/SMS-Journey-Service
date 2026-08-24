package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.config.SmsCache;
import com.vonage.smsjourneyg.dto.SmsReceivedEvent;
import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.exception.SmsNotFoundException;
import com.vonage.smsjourneyg.repository.SmsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private SmsCache smsCache;

    @InjectMocks
    private SmsService smsService;

    private Sms sms;
    private SmsRequestDto smsRequestDto;

    @BeforeEach
    void setUp() {
        sms = new Sms();
        sms.setSmsId(1L);
        sms.setRecipient("+919876543210");
        sms.setMessage("Hello World");
        sms.setStatus(SmsStatus.CREATED);
        sms.setDeleted(false);
        sms.setCreatedAt(LocalDateTime.now());

        smsRequestDto = new SmsRequestDto();
        smsRequestDto.setRecipient("+919876543210");
        smsRequestDto.setMessage("Hello World");
    }

    @Test
    void createSms_ShouldSaveAndReturnResponseDto() {
        when(smsRepository.save(any(Sms.class))).thenReturn(sms);

        SmsResponseDto result = smsService.createSms(smsRequestDto);

        assertNotNull(result);
        assertEquals(1L, result.getSmsId());
        assertEquals("+919876543210", result.getRecipient());
        assertEquals("Hello World", result.getMessage());
        assertEquals(SmsStatus.CREATED, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(smsRepository, times(1)).save(any(Sms.class));
        verify(smsCache, times(1)).invalidate();
    }

    @Test
    void getSms_WhenSmsExists_ShouldReturnResponseDto() {
        when(smsRepository.findBySmsIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(sms));

        SmsResponseDto result = smsService.getSms(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSmsId());
        assertEquals("+919876543210", result.getRecipient());
        assertEquals("Hello World", result.getMessage());
        assertEquals(SmsStatus.CREATED, result.getStatus());

        verify(smsRepository, times(1)).findBySmsIdAndDeletedIsFalse(1L);
    }

    @Test
    void getSms_WhenSmsDoesNotExist_ShouldThrowSmsNotFoundException() {
        when(smsRepository.findBySmsIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

        SmsNotFoundException exception = assertThrows(
                SmsNotFoundException.class,
                () -> smsService.getSms(1L)
        );

        assertEquals("SMS not found: 1", exception.getMessage());
        verify(smsRepository, times(1)).findBySmsIdAndDeletedIsFalse(1L);
    }

    @Test
    void getAllSms_ShouldReturnListOfSmsResponseDto() {
        SmsResponseDto smsResponseDto2 = new SmsResponseDto();
        smsResponseDto2.setSmsId(2L);
        smsResponseDto2.setRecipient("+919876543211");
        smsResponseDto2.setMessage("Second Message");
        smsResponseDto2.setStatus(SmsStatus.SENT);

        when(smsCache.getAllSms()).thenReturn(List.of(
                toDto(sms),
                smsResponseDto2
        ));

        List<SmsResponseDto> result = smsService.getAllSms();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getSmsId());
        assertEquals(2L, result.get(1).getSmsId());

        verify(smsCache, times(1)).getAllSms();
    }

    @Test
    void getAllSms_WhenNoSmsExists_ShouldReturnEmptyList() {
        when(smsCache.getAllSms()).thenReturn(List.of());

        List<SmsResponseDto> result = smsService.getAllSms();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(smsCache, times(1)).getAllSms();
    }

    @Test
    void getAllSms_WhenPageableProvided_ShouldReturnPaginatedResult() {
        when(smsCache.getAllSms()).thenReturn(List.of(
                toDto(sms),
                buildSmsResponse(2L, "+919876543211", "Second Message", SmsStatus.SENT),
                buildSmsResponse(3L, "+919876543212", "Third Message", SmsStatus.CREATED)
        ));

        Page<SmsResponseDto> result = smsService.getAllSms(PageRequest.of(1, 2));

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(1, result.getContent().size());
        assertEquals(3L, result.getContent().get(0).getSmsId());
    }

    @Test
    void getAllSms_WhenPageableOffsetBeyondSize_ShouldReturnEmptyPage() {
        when(smsCache.getAllSms()).thenReturn(List.of(toDto(sms)));

        Page<SmsResponseDto> result = smsService.getAllSms(PageRequest.of(10, 5));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteSms_WhenSmsExists_ShouldDeleteSuccessfully() {
        when(smsRepository.findBySmsIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(sms));

        assertDoesNotThrow(() -> smsService.deleteSms(1L));
        assertTrue(Boolean.TRUE.equals(sms.getDeleted()));

        verify(smsRepository, times(1)).findBySmsIdAndDeletedIsFalse(1L);
        verify(smsRepository, times(1)).save(sms);
        verify(smsCache, times(1)).invalidate();
    }

    @Test
    void deleteSms_WhenSmsDoesNotExist_ShouldThrowSmsNotFoundException() {
        when(smsRepository.findBySmsIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

        SmsNotFoundException exception = assertThrows(
                SmsNotFoundException.class,
                () -> smsService.deleteSms(1L)
        );

        assertEquals("SMS not found: 1", exception.getMessage());
        verify(smsRepository, times(1)).findBySmsIdAndDeletedIsFalse(1L);
        verify(smsRepository, never()).save(any(Sms.class));
        verify(smsCache, never()).invalidate();
    }

    @Test
    void shouldNotSaveDuplicateKafkaMessage() {

        // Arrange
        SmsReceivedEvent event =
                new SmsReceivedEvent();

        event.setSmsId("KAFKA-123");

        event.setRecipient(
                "+919876543210"
        );

        event.setMessage(
                "Hello"
        );

        Sms existingSms =
                new Sms();

        existingSms.setSmsId(1L);

        existingSms.setExternalSmsId(
                "KAFKA-123"
        );

        when(
                smsRepository
                        .findByExternalSmsId(
                                "KAFKA-123"
                        )
        ).thenReturn(
                Optional.of(existingSms)
        );

        // Act
        smsService.processIncomingSms(event);

        // Assert
        verify(
                smsRepository,
                never()
        ).save(any(Sms.class));

        verify(
                smsCache,
                never()
        ).invalidate();
    }

    @Test
    void shouldNotAllowDuplicateExternalSmsId() {

        Sms sms1 = new Sms();

        sms1.setExternalSmsId("KAFKA-123");
        sms1.setRecipient("+919876543210");
        sms1.setMessage("First");
        sms1.setStatus(SmsStatus.SCHEDULED);

        smsRepository.saveAndFlush(sms1);

        Sms sms2 = new Sms();

        sms2.setExternalSmsId("KAFKA-123");
        sms2.setRecipient("+919812345678");
        sms2.setMessage("Second");
        sms2.setStatus(SmsStatus.SCHEDULED);

        assertThrows(
                Exception.class,
                () -> smsRepository.saveAndFlush(sms2)
        );
    }


    private SmsResponseDto toDto(Sms sms) {
        SmsResponseDto dto = new SmsResponseDto();
        dto.setSmsId(sms.getSmsId());
        dto.setRecipient(sms.getRecipient());
        dto.setMessage(sms.getMessage());
        dto.setStatus(sms.getStatus());
        dto.setCreatedAt(sms.getCreatedAt());
        return dto;
    }

    private SmsResponseDto buildSmsResponse(Long smsId, String recipient, String message, SmsStatus status) {
        SmsResponseDto dto = new SmsResponseDto();
        dto.setSmsId(smsId);
        dto.setRecipient(recipient);
        dto.setMessage(message);
        dto.setStatus(status);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}