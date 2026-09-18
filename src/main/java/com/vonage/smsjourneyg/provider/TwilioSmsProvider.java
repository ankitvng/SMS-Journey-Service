package com.vonage.smsjourneyg.provider;

import com.vonage.smsjourneyg.entity.Sms;
import org.springframework.stereotype.Component;

@Component
public class TwilioSmsProvider implements SmsProvider {
    @Override
    public boolean send(Sms sms) {
        return true;
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }
}
