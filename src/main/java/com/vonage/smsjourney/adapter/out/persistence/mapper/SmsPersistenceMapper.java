package com.vonage.smsjourney.adapter.out.persistence.mapper;

import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJpaEntity;
import com.vonage.smsjourney.domain.model.Sms;
import org.springframework.stereotype.Component;

@Component
public class SmsPersistenceMapper {

    public Sms toDomain(SmsJpaEntity entity) {
        return new Sms(
            entity.getSmsId(),
            entity.getRecipient(),
            entity.getMessage(),
            entity.getStatus(),
            entity.getDeleted(),
            entity.getCreatedAt()
        );
    }

    public SmsJpaEntity toEntity(Sms sms) {
        return new SmsJpaEntity(
            sms.getId(),
            sms.getRecipient(),
            sms.getMessage(),
            sms.getStatus(),
            sms.isDeleted(),
            sms.getCreatedAt()
        );
    }
}
