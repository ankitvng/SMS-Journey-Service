package com.vonage.smsjourney.application.port.out.cache;

import com.vonage.smsjourney.domain.model.Sms;

import java.util.List;

public interface SmsCachePort {
    List<Sms> getAllSms();

    void invalidateAllSms();

    List<Sms> getSmsBySmsId(Long smsId);

}
