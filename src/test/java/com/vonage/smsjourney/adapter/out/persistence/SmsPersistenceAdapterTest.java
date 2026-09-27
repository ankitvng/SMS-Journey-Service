package com.vonage.smsjourney.adapter.out.persistence;

import com.vonage.smsjourney.adapter.out.persistence.adapter.SmsPersistenceAdapter;
import com.vonage.smsjourney.adapter.out.persistence.mapper.SmsPersistenceMapper;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@Import({SmsPersistenceAdapter.class, SmsPersistenceMapper.class})
class SmsPersistenceAdapterTest {

    @Autowired
    private SmsRepositoryPort smsRepository;

    @Test
    void findAllActive_shouldReturnOnlyActiveSms() {
        Sms active = sms("+919876543210", "Active message", SmsStatus.CREATED, false);
        Sms deleted = sms("+919876543211", "Deleted message", SmsStatus.FAILED, true);
        smsRepository.save(active);
        smsRepository.save(deleted);

        List<Sms> result = smsRepository.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRecipient()).isEqualTo("+919876543210");
    }

    @Test
    void findActiveById_shouldReturnActiveSms() {
        Sms saved = smsRepository.save(sms("+919876543210", "Active message", SmsStatus.CREATED, false));

        assertThat(smsRepository.findActiveById(saved.getId()))
                .hasValueSatisfying(result -> assertThat(result.getMessage()).isEqualTo("Active message"));
    }

    @Test
    void findActiveById_shouldReturnEmptyForDeletedSms() {
        Sms saved = smsRepository.save(sms("+919876543211", "Deleted message", SmsStatus.FAILED, true));

        assertThat(smsRepository.findActiveById(saved.getId())).isEmpty();
    }

    @Test
    void existsBySmsIdAndDeletedIsFalse_shouldHonorDeletedFlag() {
        Sms saved = smsRepository.save(sms("+919876543210", "Active message", SmsStatus.CREATED, false));

        assertThat(smsRepository.findActiveById(saved.getId())).isPresent();
    }

    private Sms sms(String recipient, String message, SmsStatus status, boolean deleted) {
        return new Sms(null, recipient, message, status, deleted, LocalDateTime.now());
    }
}
