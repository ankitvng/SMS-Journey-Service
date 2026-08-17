package com.vonage.smsjourneyg.entity;

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

    @Column(columnDefinition = "TEXT")
    private String smsBody;

    private int charCount;
    private int segments;
    private boolean isUnicode;
    private String routingStep;
    private String primaryRoute;
    private String fallbackRoute;
    private double cost;
    private String status = "Scheduled";
    private LocalDateTime scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sms_id")
    private Sms sms;
}
//smsId, routingStep,Status, Time,
//sms entity (O-M)one msg can have multiple routes/journeys