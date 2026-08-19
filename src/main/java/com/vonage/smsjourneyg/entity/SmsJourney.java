package com.vonage.smsjourneyg.entity;

import com.vonage.smsjourneyg.enums.SmsStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class SmsJourney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String campaignName;

//    @Column(columnDefinition = "TEXT")
//    private String smsBody;
//
    private String routingStep;
    private String primaryRoute;
    private String fallbackRoute;
    private double cost;

    @Enumerated
    private SmsStatus status = SmsStatus.SCHEDULED;

    private LocalDateTime scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sms_id")
    private Sms sms;
}
//smsId, routingStep,Status, Time,
//sms entity (O-M)one msg can have multiple routes/journeys