package com.vonage.smsjourney.adapter.out.cache;

import com.vonage.smsjourney.adapter.out.cache.SmsCacheAdapter;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SmsCacheAdapterTest {

    @Mock
    private SmsRepositoryPort smsRepository;

    private SmsCacheAdapter smsCache;

    @BeforeEach
    void setUp() {
        smsCache = new SmsCacheAdapter(smsRepository, Duration.ofMinutes(10));
    }

    @Test
    void getAllSms_onCacheMiss_loadsFromRepository() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms(1L, "Alice", "Hello")));

        List<Sms> result = smsCache.getAllSms();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getRecipient()).isEqualTo("Alice");
        then(smsRepository).should().findAllActive();
    }

    @Test
    void getAllSms_onSubsequentCalls_doesNotHitRepositoryAgain() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms(1L, "Alice", "Hello")));

        smsCache.getAllSms();
        smsCache.getAllSms();
        smsCache.getAllSms();

        then(smsRepository).should().findAllActive();
    }

    @Test
    void invalidate_forcesNextGetAllSms_toReloadFromRepository() {
        given(smsRepository.findAllActive())
                .willReturn(List.of(sms(1L, "Alice", "Hello")))
                .willReturn(List.of(sms(1L, "Alice", "Hello"), sms(2L, "Bob", "Hi")));

        assertThat(smsCache.getAllSms()).hasSize(1);
        smsCache.invalidateAllSms();

        assertThat(smsCache.getAllSms()).hasSize(2);
        then(smsRepository).should(org.mockito.Mockito.times(2)).findAllActive();
    }

    @Test
    void warmUpCache_populatesCache_beforeAnyGetAllSmsCall() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms(1L, "Alice", "Hello")));

        smsCache.warmUp();

        then(smsRepository).should().findAllActive();
        assertThat(smsCache.getAllSms()).hasSize(1);
        then(smsRepository).should().findAllActive();
    }

    @Test
    void getAllSms_returnsAllSmsFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        Sms expected = new Sms(5L, "Carol", "Test message", SmsStatus.CREATED, false, createdAt);
        given(smsRepository.findAllActive()).willReturn(List.of(expected));

        Sms result = smsCache.getAllSms().getFirst();

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void getAllSms_whenRepositoryReturnsEmptyList_returnsEmptyCacheResult() {
        given(smsRepository.findAllActive()).willReturn(List.of());

        assertThat(smsCache.getAllSms()).isEmpty();
        then(smsRepository).should().findAllActive();
    }

    @Test
    void firstCallShouldFetchFromRepositoryAndSecondCallShouldUseCache() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms(1L, "+919876543210", "Hello")));

        List<Sms> firstCall = smsCache.getAllSms();
        List<Sms> secondCall = smsCache.getAllSms();

        assertThat(firstCall).hasSize(1).isEqualTo(secondCall);
        then(smsRepository).should().findAllActive();
    }

    @Test
    void shouldCallRepositoryAgainAfterCacheInvalidation() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms(1L, "+919876543210", "Hello")));

        smsCache.getAllSms();
        smsCache.invalidateAllSms();
        smsCache.getAllSms();

        then(smsRepository).should(org.mockito.Mockito.times(2)).findAllActive();
    }

    private Sms sms(Long id, String recipient, String message) {
        return new Sms(id, recipient, message, SmsStatus.CREATED, false, LocalDateTime.now());
    }
}
