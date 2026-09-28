package com.vonage.smsjourney.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sms {
    private Long id;
    private String recipient;
    private String message;
    private SmsStatus status;
    private boolean deleted;
    private LocalDateTime createdAt;
    private List<SmsJourney> journeys;

    public Sms(Long smsId, String recipient, String message, SmsStatus status, Boolean deleted, LocalDateTime createdAt) {
        this.id = smsId;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public static Sms create(String recipient, String message, Clock clock) {
        Sms sms = new Sms();
        sms.recipient = recipient;
        sms.message = message;
        sms.status = SmsStatus.CREATED;
        sms.createdAt = LocalDateTime.now(clock);
        sms.deleted = false;
        return sms;
    }

    public void delete() {
        this.deleted = true;
    }
}
