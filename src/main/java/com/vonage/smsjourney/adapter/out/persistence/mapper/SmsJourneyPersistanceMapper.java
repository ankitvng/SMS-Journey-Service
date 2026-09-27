package com.vonage.smsjourney.adapter.out.persistence.mapper;

import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJourneyJpaEntity;
import com.vonage.smsjourney.domain.model.SmsJourney;
import org.springframework.stereotype.Component;

@Component
public class SmsJourneyPersistanceMapper {
    public SmsJourney toDomain(SmsJourneyJpaEntity smsJourney) {
        return new SmsJourney(
                smsJourney.getId(),
                smsJourney.getCampaignName(),
                smsJourney.getRoutingStep(),
                smsJourney.getPrimaryRoute(),
                smsJourney.getFallbackRoute(),
                smsJourney.getCost(),
                smsJourney.getStatus(),
                smsJourney.getScheduledTime(),
                new SmsPersistenceMapper().toDomain(smsJourney.getSms())
        );
    }

    public SmsJourneyJpaEntity toJpaEntity(SmsJourney domain) {
        return new SmsJourneyJpaEntity(
                domain.getId(),
                domain.getCampaignName(),
                domain.getRoutingStep(),
                domain.getPrimaryRoute(),
                domain.getFallbackRoute(),
                domain.getCost(),
                domain.getStatus(),
                domain.getScheduledTime(),
                new SmsPersistenceMapper().toEntity(domain.getSms())
        );
    }
}
