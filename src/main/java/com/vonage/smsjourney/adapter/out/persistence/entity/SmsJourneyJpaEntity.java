package com.vonage.smsjourney.adapter.out.persistence.entity;

import com.vonage.smsjourney.domain.model.SmsStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sms_journeys")
@AllArgsConstructor
@NoArgsConstructor
public class SmsJourneyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String campaignName;

    private String routingStep;
    private String primaryRoute;
    private String fallbackRoute;
    private double cost;

    @Enumerated(EnumType.STRING)
    private SmsStatus status;

    private LocalDateTime scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sms_id")
    private SmsJpaEntity sms;

}
