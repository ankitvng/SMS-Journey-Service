package com.vonage.smsjourneyg.repository;

import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SmsRepositoryTest {

    @Autowired
    private SmsRepository smsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE sms_journey, sms RESTART IDENTITY CASCADE");
    }

    @Test
    void findAllByDeletedIsFalse_shouldReturnOnlyActiveSms() {
        Sms active = new Sms();
        active.setRecipient("+919876543210");
        active.setMessage("Active message");
        active.setStatus(SmsStatus.CREATED);
        active.setDeleted(false);
        active.setCreatedAt(LocalDateTime.now());

        Sms deleted = new Sms();
        deleted.setRecipient("+919876543211");
        deleted.setMessage("Deleted message");
        deleted.setStatus(SmsStatus.FAILED);
        deleted.setDeleted(true);
        deleted.setCreatedAt(LocalDateTime.now());

        smsRepository.saveAll(List.of(active, deleted));

        List<Sms> result = smsRepository.findAllByDeletedIsFalse();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipient()).isEqualTo("+919876543210");
    }

    @Test
    void findBySmsIdAndDeletedIsFalse_shouldReturnActiveSmsById() {
        Sms active = new Sms();
        active.setRecipient("+919876543210");
        active.setMessage("Active message");
        active.setStatus(SmsStatus.CREATED);
        active.setDeleted(false);
        active.setCreatedAt(LocalDateTime.now());

        Sms saved = smsRepository.save(active);

        assertThat(smsRepository.findBySmsIdAndDeletedIsFalse(saved.getSmsId())).isPresent();
        assertThat(smsRepository.findBySmsIdAndDeletedIsFalse(saved.getSmsId()).get().getMessage()).isEqualTo("Active message");
    }

    @Test
    void findBySmsIdAndDeletedIsFalse_shouldReturnEmptyForDeletedSms() {
        Sms deleted = new Sms();
        deleted.setRecipient("+919876543211");
        deleted.setMessage("Deleted message");
        deleted.setStatus(SmsStatus.FAILED);
        deleted.setDeleted(true);
        deleted.setCreatedAt(LocalDateTime.now());

        Sms saved = smsRepository.save(deleted);

        assertThat(smsRepository.findBySmsIdAndDeletedIsFalse(saved.getSmsId())).isEmpty();
    }

    @Test
    void existsBySmsIdAndDeletedIsFalse_shouldHonorDeletedFlag() {
        Sms active = new Sms();
        active.setRecipient("+919876543210");
        active.setMessage("Active message");
        active.setStatus(SmsStatus.CREATED);
        active.setDeleted(false);
        active.setCreatedAt(LocalDateTime.now());

        Sms saved = smsRepository.save(active);

        assertThat(smsRepository.existsBySmsIdAndDeletedIsFalse(saved.getSmsId())).isTrue();
    }

}
