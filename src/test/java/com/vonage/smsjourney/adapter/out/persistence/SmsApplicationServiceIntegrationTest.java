package com.vonage.smsjourney.adapter.out.persistence;

import com.vonage.smsjourney.adapter.out.persistence.adapter.SmsPersistenceAdapter;
import com.vonage.smsjourney.adapter.out.persistence.mapper.SmsPersistenceMapper;
import com.vonage.smsjourney.application.port.in.DeleteSmsUseCase;
import com.vonage.smsjourney.application.port.out.cache.SmsCachePort;
import com.vonage.smsjourney.application.port.out.cache.SmsJourneyCachePort;
import com.vonage.smsjourney.application.port.out.messaging.SmsEventPublisherPort;
import com.vonage.smsjourney.application.port.out.repository.SmsJourneyRepositoryPort;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.application.service.SmsApplicationService;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@Import({SmsApplicationService.class, SmsPersistenceAdapter.class, SmsPersistenceMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SmsApplicationServiceIntegrationTest {

    @Autowired
    private SmsRepositoryPort smsRepository;
    @Autowired
    private DeleteSmsUseCase deleteSms;
    @MockitoBean
    private SmsJourneyRepositoryPort journeyRepository;
    @MockitoBean
    private SmsCachePort smsCache;
    @MockitoBean
    private SmsJourneyCachePort journeyCache;
    @MockitoBean
    private SmsEventPublisherPort eventPublisher;

    @Test
    void deleteSms_whenJourneyDeleteFails_rollsBackSmsDeletion() {
        Sms sms = new Sms(null, "+1234567890", "Test message", SmsStatus.CREATED,
                false, LocalDateTime.now());
        Sms saved = smsRepository.save(sms);
        doThrow(new RuntimeException("journey deletion failed"))
                .when(journeyRepository).deleteBySmsSmsId(saved.getId());

        assertThatThrownBy(() -> deleteSms.delete(saved.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("journey deletion failed");

        assertThat(smsRepository.findActiveById(saved.getId()))
                .hasValueSatisfying(activeSms -> assertThat(activeSms.isDeleted()).isFalse())
                .as("SMS should not be deleted due to rollback");
        then(smsCache).should(never()).invalidateAllSms();
    }
}
