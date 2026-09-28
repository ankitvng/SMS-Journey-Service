package com.vonage.smsjourney.application;

import com.vonage.smsjourney.adapter.out.cache.SmsJourneyCacheAdapter;
import com.vonage.smsjourney.application.port.in.CreateSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.application.service.SmsJourneyApplicationService;
import com.vonage.smsjourney.application.service.mapper.SmsJourneyMapper;
import com.vonage.smsjourney.domain.exception.SmsNotFoundException;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsJourney;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SmsJourneyApplicationServiceTest {

    @Mock
    private SmsJourneyRepositoryPort journeyRepository;
    @Mock
    private SmsRepositoryPort smsRepository;

    private CreateSmsJourneyUseCase createJourney;
    private GetSmsJourneyUseCase getJourneys;
    private Sms sms;
    private SmsJourney journey;
    private CreateSmsJourneyCommand command;

    @BeforeEach
    void setUp() {
        SmsJourneyApplicationService applicationService = new SmsJourneyApplicationService(journeyRepository, smsRepository, new SmsJourneyMapper(),new SmsJourneyCacheAdapter(journeyRepository, Duration.ofSeconds(5)));
        createJourney = applicationService;
        getJourneys = applicationService;

        sms = new Sms(100L, "+919876543210", "Test Message", SmsStatus.CREATED, false, LocalDateTime.now());
        journey = new SmsJourney(1L, "Summer Promo", "PRIMARY_ATTEMPT", "ROUTE_A", "ROUTE_B", 0.05, SmsStatus.SCHEDULED, null, sms);
        command = new CreateSmsJourneyCommand("Summer Promo", "PRIMARY_ATTEMPT", "ROUTE_A", "ROUTE_B", 0.05);
    }

    @Test
    void createJourney_whenSmsExists_createsAndReturnsJourney() {
        given(smsRepository.findActiveById(100L)).willReturn(Optional.of(sms));
        given(journeyRepository.save(any(SmsJourney.class))).willReturn(journey);

        SmsJourney result = createJourney.createSmsJourney(100L, command);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCampaignName()).isEqualTo("Summer Promo");
        assertThat(result.getRoutingStep()).isEqualTo("PRIMARY_ATTEMPT");
        assertThat(result.getPrimaryRoute()).isEqualTo("ROUTE_A");
        assertThat(result.getFallbackRoute()).isEqualTo("ROUTE_B");
        assertThat(result.getCost()).isEqualTo(0.05);
        assertThat(result.getStatus()).isEqualTo(SmsStatus.SCHEDULED);
        then(smsRepository).should().findActiveById(100L);
        then(journeyRepository).should().save(any(SmsJourney.class));
    }

    @Test
    void createJourney_whenSmsDoesNotExist_throwsNotFound() {
        given(smsRepository.findActiveById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> createJourney.createSmsJourney(100L, command)).isInstanceOf(SmsNotFoundException.class).hasMessage("SMS not found: 100");
        then(journeyRepository).should(never()).save(any());
    }

    @Test
    void getJourneysBySms_whenSmsExists_returnsJourneyList() {
        given(smsRepository.findActiveById(100L)).willReturn(Optional.of(sms));
        given(journeyRepository.findBySmsId(100L)).willReturn(List.of(journey));

        List<SmsJourney> result = getJourneys.getById(100L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getCampaignName()).isEqualTo("Summer Promo");
        then(journeyRepository).should().findBySmsId(100L);
    }

    @Test
    void getJourneysBySms_whenSmsDoesNotExist_throwsNotFound() {
        given(smsRepository.findActiveById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getJourneys.getById(100L)).isInstanceOf(SmsNotFoundException.class).hasMessage("SMS not found: 100");
        then(journeyRepository).should(never()).findBySmsId(100L);
    }

    @Test
    void getJourneysBySms_whenSmsHasNoJourneys_returnsEmptyList() {
        given(smsRepository.findActiveById(100L)).willReturn(Optional.of(sms));
        given(journeyRepository.findBySmsId(100L)).willReturn(List.of());

        assertThat(getJourneys.getById(100L)).isEmpty();
        then(journeyRepository).should().findBySmsId(100L);
    }
}
