package com.vonage.smsjourneyg.provider;

import com.vonage.smsjourneyg.entity.Sms;

public interface SmsProvider {
    boolean send(Sms sms);

    String getProviderName();
}
