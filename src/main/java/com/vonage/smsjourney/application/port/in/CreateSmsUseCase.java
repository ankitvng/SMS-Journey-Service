package com.vonage.smsjourney.application.port.in;


import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.domain.model.Sms;

public interface CreateSmsUseCase {
    Sms create(SendSmsCommand request);
}
