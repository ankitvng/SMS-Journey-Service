package com.vonage.smsjourney.adapter.out.persistence.entity;

import com.vonage.smsjourney.domain.model.SmsStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sms")
public class SmsJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smsId;

    private String recipient;
    private String message;

    @Enumerated(EnumType.STRING)
    private SmsStatus status;
    private Boolean deleted;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "sms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SmsJourneyJpaEntity> journeys = new ArrayList<>();

    public SmsJpaEntity(Long smsId, String recipient, String message, SmsStatus status,
                        Boolean deleted, LocalDateTime createdAt) {
        this.smsId = smsId;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }
}
