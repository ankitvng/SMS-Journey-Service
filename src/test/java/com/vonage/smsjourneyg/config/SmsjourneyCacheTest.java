package com.vonage.smsjourneyg.config;

import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsJourneyCacheTest {

    @Mock
    private SmsJourneyRepository repository;

    private SmsJourneyCache smsJourneyCache;

    @BeforeEach
    void setUp() {

        smsJourneyCache =
                new SmsJourneyCache(repository);
    }

    @Test
    void shouldFetchFromRepositoryOnlyOnce() {

        // Arrange
        SmsJourney journey1 = new SmsJourney();

        journey1.setId(1L);
        journey1.setStatus(SmsStatus.SENT);
        journey1.setPrimaryRoute("VONAGE");

        SmsJourney journey2 = new SmsJourney();

        journey2.setId(2L);
        journey2.setStatus(SmsStatus.SCHEDULED);
        journey2.setPrimaryRoute("TWILIO");

        when(repository.findBySmsSmsId(101L))
                .thenReturn(
                        List.of(
                                journey1,
                                journey2
                        )
                );

        // First call -> repository
        var firstResult =
                smsJourneyCache
                        .getJourneysBySmsId(101L);

        // Second call -> cache
        var secondResult =
                smsJourneyCache
                        .getJourneysBySmsId(101L);

        // Third call -> cache
        var thirdResult =
                smsJourneyCache
                        .getJourneysBySmsId(101L);

        assertEquals(2, firstResult.size());
        assertEquals(2, secondResult.size());
        assertEquals(2, thirdResult.size());

        // Most important assertion
        verify(
                repository,
                times(1)
        ).findBySmsSmsId(101L);
    }

    @Test
    void shouldFetchAgainAfterInvalidation() {

        SmsJourney journey = new SmsJourney();

        journey.setId(1L);
        journey.setStatus(SmsStatus.SENT);

        when(repository.findBySmsSmsId(101L))
                .thenReturn(List.of(journey));

        // DB
        smsJourneyCache
                .getJourneysBySmsId(101L);

        // Cache
        smsJourneyCache
                .getJourneysBySmsId(101L);

        // Remove cached value
        smsJourneyCache.invalidate(101L);

        // DB again
        smsJourneyCache
                .getJourneysBySmsId(101L);

        verify(
                repository,
                times(2)
        ).findBySmsSmsId(101L);
    }
}