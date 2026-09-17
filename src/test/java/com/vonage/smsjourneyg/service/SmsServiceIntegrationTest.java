
package com.vonage.smsjourneyg.service;
import com.vonage.smsjourneyg.config.SmsCache;
import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import com.vonage.smsjourneyg.repository.SmsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@Slf4j
@SpringBootTest
public class SmsServiceIntegrationTest {

    @Autowired
    private SmsService smsService;

    @Autowired
    private SmsRepository smsRepository;

    @MockitoBean
    private SmsJourneyRepository journeyRepository;

    @MockitoBean
    private SmsCache smsCache;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void deleteSms_WhenJourneyDeleteFails_ShouldRollbackSmsDeletion() {
        Sms sms = new Sms();
        sms.setSmsId(1L);
        sms.setRecipient("+1234567890");
        sms.setMessage("Test message");
        sms.setCreatedAt(java.time.LocalDateTime.now());
        sms.setStatus(SmsStatus.CREATED);
        sms.setDeleted(false);

        sms = smsRepository.save(sms);

        doThrow(new RuntimeException("journey deletion failed")).when(journeyRepository).deleteBySmsSmsId(sms.getSmsId());

        Exception exception = assertThrows(RuntimeException.class, () -> smsService.deleteSms(1L));

        assertEquals("journey deletion failed", exception.getMessage(), "Expected exception");

        entityManager.clear();

        assertTrue(smsRepository.existsBySmsIdAndDeletedIsFalse(1L), "SMS should not be deleted due to rollback");

        verify(smsCache,never()).invalidate();
    }

}
