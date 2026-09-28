package com.vonage.smsjourney.application;

import com.vonage.smsjourney.application.port.in.CreateSmsUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsUseCase;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.application.port.out.cache.SmsCachePort;
import com.vonage.smsjourney.application.port.out.cache.SmsJourneyCachePort;
import com.vonage.smsjourney.application.port.out.messaging.SmsEventPublisherPort;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.application.service.SmsApplicationService;
import com.vonage.smsjourney.domain.exception.SmsNotFoundException;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SmsApplicationServiceTest {

    @Mock
    private SmsRepositoryPort smsRepository;
    @Mock
    private SmsJourneyRepositoryPort journeyRepository;
    @Mock
    private SmsEventPublisherPort eventPublisher;
    @Mock
    private SmsCachePort smsCache;
    @Mock
    private SmsJourneyCachePort journeyCache;

    private CreateSmsUseCase createSms;
    private GetSmsUseCase getSms;
    private DeleteSmsUseCase deleteSms;
    private Sms sms;

    @BeforeEach
    void setUp() {
        SmsApplicationService applicationService = new SmsApplicationService(
                smsRepository, journeyRepository, eventPublisher, smsCache, journeyCache);
        createSms = applicationService;
        getSms = applicationService;
        deleteSms = applicationService;

        sms = new Sms(1L, "+919876543210", "Hello World", SmsStatus.CREATED,
                false, LocalDateTime.now());
    }

    @Test
    void createSms_savesAndReturnsSms() {
        given(smsRepository.save(any(Sms.class))).willReturn(sms);

        Sms result = createSms.create(new SendSmsCommand(
                sms.getRecipient(), sms.getMessage(), new CreateSmsJourneyCommand(
                "Summer Promo", "PRIMARY_ATTEMPT", "ROUTE_A", "ROUTE_B", 0.05)));

        assertThat(result).usingRecursiveComparison().isEqualTo(sms);
        then(smsRepository).should().save(any(Sms.class));
        then(smsCache).should().invalidateAllSms();
        then(eventPublisher).should().publish(any());
    }

    @Test
    void getSms_whenSmsExists_returnsSms() {
        given(smsCache.getSmsBySmsId(1L)).willReturn(List.of(sms));

        Sms result = getSms.getById(1L);

        assertThat(result).usingRecursiveComparison().isEqualTo(sms);
        then(smsCache).should().getSmsBySmsId(1L);
    }

    @Test
    void getSms_whenSmsDoesNotExist_throwsNotFound() {
        given(smsCache.getSmsBySmsId(1L)).willReturn(List.of());

        assertThatThrownBy(() -> getSms.getById(1L))
                .isInstanceOf(SmsNotFoundException.class)
                .hasMessage("SMS not found: 1");
        then(smsCache).should().getSmsBySmsId(1L);
    }

    @Test
    void getAllSms_returnsAllActiveSms() {
        Sms secondSms = new Sms(2L, "+919876543211", "Second Message", SmsStatus.SENT,
                false, LocalDateTime.now());
        given(smsRepository.findAllActive()).willReturn(List.of(sms, secondSms));

        List<Sms> result = getSms.getAllPaged(0, 10);

        assertThat(result).extracting(Sms::getId).containsExactly(1L, 2L);
        then(smsRepository).should().findAllActive();
    }

    @Test
    void getAllSms_whenNoSmsExists_returnsEmptyList() {
        given(smsRepository.findAllActive()).willReturn(List.of());

        assertThat(getSms.getAllPaged(0, 10)).isEmpty();
        then(smsRepository).should().findAllActive();
    }

    @Test
    void getAllSms_whenPageableProvided_returnsRequestedPage() {
        Sms second = new Sms(2L, "second", "second", SmsStatus.SENT, false, LocalDateTime.now());
        Sms third = new Sms(3L, "third", "third", SmsStatus.CREATED, false, LocalDateTime.now());
        given(smsRepository.findAllActive()).willReturn(List.of(sms, second, third));

        List<Sms> result = getSms.getAllPaged(1, 2);

        assertThat(result).extracting(Sms::getId).containsExactly(3L);
    }

    @Test
    void getAllSms_whenPageOffsetIsBeyondSize_returnsEmptyList() {
        given(smsRepository.findAllActive()).willReturn(List.of(sms));

        assertThat(getSms.getAllPaged(10, 5)).isEmpty();
    }

    @Test
    void deleteSms_whenSmsExists_softDeletesSuccessfully() {
        given(smsRepository.findActiveById(1L)).willReturn(Optional.of(sms));

        deleteSms.delete(1L);

        assertThat(sms.isDeleted()).isTrue();
        then(smsRepository).should().save(sms);
        then(smsCache).should().invalidateAllSms();
    }

    @Test
    void deleteSms_whenSmsDoesNotExist_throwsNotFound() {
        given(smsRepository.findActiveById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> deleteSms.delete(1L))
                .isInstanceOf(SmsNotFoundException.class)
                .hasMessage("SMS not found: 1");
        then(smsRepository).should(never()).save(any(Sms.class));
        then(smsCache).shouldHaveNoInteractions();
    }

    @Test
    void deleteSms_whenJourneyDeleteFails_doesNotInvalidateCaches() {
        given(smsRepository.findActiveById(1L)).willReturn(Optional.of(sms));
        doThrow(new RuntimeException("Journey delete failed"))
                .when(journeyRepository).deleteBySmsSmsId(1L);

        assertThatThrownBy(() -> deleteSms.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Journey delete failed");
        then(smsRepository).should().save(sms);
        then(smsCache).shouldHaveNoInteractions();
        then(journeyCache).shouldHaveNoInteractions();
    }
}
