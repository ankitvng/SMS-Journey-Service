package com.vonage.smsjourneyg.entity;

import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Sms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smsId;

    private String recipient;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status;

    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "sms",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SmsJourney> journeys = new ArrayList<>();
}
