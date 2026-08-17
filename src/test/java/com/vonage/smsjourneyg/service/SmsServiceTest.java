package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
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
class SmsServiceTest {

    @Mock
    private SmsRepository smsRepository;

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
        sms.setStatus("CREATED");
        sms.setCreatedAt(LocalDateTime.now());

        smsRequestDto = new SmsRequestDto();
        smsRequestDto.setRecipient("+919876543210");
        smsRequestDto.setMessage("Hello World");
    }

    // --- createSms Tests ---

    @Test
    void createSms_ShouldSaveAndReturnResponseDto() {
        when(smsRepository.save(any(Sms.class))).thenReturn(sms);

        SmsResponseDto result = smsService.createSms(smsRequestDto);

        assertNotNull(result);
        assertEquals(1L, result.getSmsId());
        assertEquals("+919876543210", result.getRecipient());
        assertEquals("Hello World", result.getMessage());
        assertEquals("CREATED", result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(smsRepository, times(1)).save(any(Sms.class));
    }

    // --- getSms Tests ---

    @Test
    void getSms_WhenSmsExists_ShouldReturnResponseDto() {
        when(smsRepository.findById(1L)).thenReturn(Optional.of(sms));

        SmsResponseDto result = smsService.getSms(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSmsId());
        assertEquals("+919876543210", result.getRecipient());
        assertEquals("Hello World", result.getMessage());
        assertEquals("CREATED", result.getStatus());

        verify(smsRepository, times(1)).findById(1L);
    }

    @Test
    void getSms_WhenSmsDoesNotExist_ShouldThrowRuntimeException() {
        when(smsRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> smsService.getSms(1L)
        );

        assertEquals("SMS not found: 1", exception.getMessage());
        verify(smsRepository, times(1)).findById(1L);
    }

    // --- getAllSms Tests ---

    @Test
    void getAllSms_ShouldReturnListOfSmsResponseDto() {
        Sms sms2 = new Sms();
        sms2.setSmsId(2L);
        sms2.setRecipient("+919876543211");
        sms2.setMessage("Second Message");
        sms2.setStatus("SENT");
        sms2.setCreatedAt(LocalDateTime.now());

        when(smsRepository.findAll()).thenReturn(List.of(sms, sms2));

        List<SmsResponseDto> result = smsService.getAllSms();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getSmsId());
        assertEquals(2L, result.get(1).getSmsId());

        verify(smsRepository, times(1)).findAll();
    }

    @Test
    void getAllSms_WhenNoSmsExists_ShouldReturnEmptyList() {
        when(smsRepository.findAll()).thenReturn(List.of());

        List<SmsResponseDto> result = smsService.getAllSms();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(smsRepository, times(1)).findAll();
    }

    // --- deleteSms Tests ---

    @Test
    void deleteSms_WhenSmsExists_ShouldDeleteSuccessfully() {
        when(smsRepository.existsById(1L)).thenReturn(true);
        doNothing().when(smsRepository).deleteById(1L);

        assertDoesNotThrow(() -> smsService.deleteSms(1L));

        verify(smsRepository, times(1)).existsById(1L);
        verify(smsRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteSms_WhenSmsDoesNotExist_ShouldThrowRuntimeException() {
        when(smsRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> smsService.deleteSms(1L)
        );

        assertEquals("SMS not found: 1", exception.getMessage());
        verify(smsRepository, times(1)).existsById(1L);
        verify(smsRepository, never()).deleteById(anyLong());
    }
}