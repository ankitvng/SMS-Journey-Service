package com.vonage.smsjourney.adapter.out.cache;

import com.vonage.smsjourney.adapter.out.cache.SmsJourneyCacheAdapter;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.domain.model.SmsJourney;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SmsJourneyCacheAdapterTest {

    @Mock
    private SmsJourneyRepositoryPort journeyRepository;

    private SmsJourneyCacheAdapter journeyCache;

    @BeforeEach
    void setUp() {
        journeyCache = new SmsJourneyCacheAdapter(journeyRepository, Duration.ofMinutes(10));
    }

    @Test
    void shouldFetchFromRepositoryOnlyOnce() {
        List<SmsJourney> journeys = List.of(journey(1L, SmsStatus.SENT), journey(2L, SmsStatus.SCHEDULED));
        given(journeyRepository.findBySmsId(101L)).willReturn(journeys);

        List<SmsJourney> first = journeyCache.getJourneysBySmsId(101L);
        List<SmsJourney> second = journeyCache.getJourneysBySmsId(101L);
        List<SmsJourney> third = journeyCache.getJourneysBySmsId(101L);

        assertThat(first).hasSize(2);
        assertThat(second).hasSize(2);
        assertThat(third).hasSize(2);
        then(journeyRepository).should().findBySmsId(101L);
    }

    @Test
    void shouldFetchAgainAfterInvalidation() {
        given(journeyRepository.findBySmsId(101L)).willReturn(List.of(journey(1L, SmsStatus.SENT)));

        journeyCache.getJourneysBySmsId(101L);
        journeyCache.getJourneysBySmsId(101L);
        journeyCache.invalidate(101L);
        journeyCache.getJourneysBySmsId(101L);

        then(journeyRepository).should(org.mockito.Mockito.times(2)).findBySmsId(101L);
    }

    private SmsJourney journey(Long id, SmsStatus status) {
        return new SmsJourney(id, null, null, null, null, 0, status, null, null);
    }
}
