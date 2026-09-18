package com.vonage.smsjourneyg.config;

import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.repository.SmsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsCacheTest {

    @Mock
    private SmsRepository smsRepository;

    private SmsCache smsCache;

    @BeforeEach
    void setUp() {
        smsCache = new SmsCache(smsRepository, java.time.Duration.ofMinutes(10));
    }

    @Test
    void getAllSms_onCacheMiss_loadsFromRepository() {
        Sms sms = buildSms(1L, "Alice", "Hello");
        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(sms));

        List<SmsResponseDto> result = smsCache.getAllSms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSmsId()).isEqualTo(1L);
        assertThat(result.get(0).getRecipient()).isEqualTo("Alice");
        verify(smsRepository, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllSms_onSubsequentCalls_doesNotHitRepositoryAgain() {
        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(buildSms(1L, "Alice", "Hello")));

        smsCache.getAllSms();
        smsCache.getAllSms();
        smsCache.getAllSms();

        // repository should only be queried once — the rest are cache hits
        verify(smsRepository, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void invalidate_forcesNextGetAllSms_toReloadFromRepository() {
        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(buildSms(1L, "Alice", "Hello")))
                .thenReturn(List.of(
                        buildSms(1L, "Alice", "Hello"),
                        buildSms(2L, "Bob", "Hi")
                ));

        List<SmsResponseDto> firstCall = smsCache.getAllSms();
        assertThat(firstCall).hasSize(1);

        smsCache.invalidate();

        List<SmsResponseDto> secondCall = smsCache.getAllSms();
        assertThat(secondCall).hasSize(2);

        verify(smsRepository, times(2)).findAllByDeletedIsFalse();
    }

    @Test
    void warmUpCache_populatesCache_beforeAnyGetAllSmsCall() {
        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(buildSms(1L, "Alice", "Hello")));

        smsCache.warmUpCache();

        // repository already hit once during warm-up
        verify(smsRepository, times(1)).findAllByDeletedIsFalse();

        List<SmsResponseDto> result = smsCache.getAllSms();

        assertThat(result).hasSize(1);
        // still only 1 call total — getAllSms() was served from the warm cache
        verify(smsRepository, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void getAllSms_mapsAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Sms sms = new Sms();
        sms.setSmsId(5L);
        sms.setRecipient("Carol");
        sms.setMessage("Test message");
        sms.setStatus(SmsStatus.CREATED);
        sms.setCreatedAt(now);

        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(sms));

        SmsResponseDto dto = smsCache.getAllSms().get(0);

        assertThat(dto.getSmsId()).isEqualTo(5L);
        assertThat(dto.getRecipient()).isEqualTo("Carol");
        assertThat(dto.getMessage()).isEqualTo("Test message");
        assertThat(dto.getStatus()).isEqualTo(SmsStatus.CREATED);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void getAllSms_whenRepositoryReturnsEmptyList_returnsEmptyCacheResult() {
        when(smsRepository.findAllByDeletedIsFalse()).thenReturn(List.of());

        List<SmsResponseDto> result = smsCache.getAllSms();

        assertThat(result).isEmpty();
        verify(smsRepository, times(1)).findAllByDeletedIsFalse();
    }

    @Test
    void firstCallShouldFetchFromRepositoryAndSecondCallShouldUseCache() {

        // Arrange
        Sms sms = new Sms();

        sms.setSmsId(1L);
        sms.setRecipient("+919876543210");
        sms.setMessage("Hello");
        sms.setStatus(SmsStatus.SCHEDULED);

        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(sms));

        // Act
        List<SmsResponseDto> firstCall =
                smsCache.getAllSms();

        List<SmsResponseDto> secondCall =
                smsCache.getAllSms();

        // Assert
        assertEquals(1, firstCall.size());
        assertEquals(1, secondCall.size());

        assertEquals(
                firstCall,
                secondCall
        );

        // Repository must only be called once
        verify(
                smsRepository,
                times(1)
        ).findAllByDeletedIsFalse();
    }

    @Test
    void shouldCallRepositoryAgainAfterCacheInvalidation() {

        // Arrange
        Sms sms = new Sms();

        sms.setSmsId(1L);
        sms.setRecipient("+919876543210");
        sms.setMessage("Hello");
        sms.setStatus(SmsStatus.SCHEDULED);

        when(smsRepository.findAllByDeletedIsFalse())
                .thenReturn(List.of(sms));

        // Act
        smsCache.getAllSms();

        smsCache.invalidate();

        smsCache.getAllSms();

        // Assert
        verify(
                smsRepository,
                times(2)
        ).findAllByDeletedIsFalse();
    }

    private Sms buildSms(Long id, String recipient, String message) {
        Sms sms = new Sms();
        sms.setSmsId(id);
        sms.setRecipient(recipient);
        sms.setMessage(message);
        sms.setStatus(SmsStatus.CREATED);
        sms.setCreatedAt(LocalDateTime.now());
        return sms;
    }
}