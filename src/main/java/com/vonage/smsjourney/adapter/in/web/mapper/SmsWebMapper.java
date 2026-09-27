package com.vonage.smsjourney.adapter.in.web.mapper;


import com.vonage.smsjourney.adapter.in.web.dto.SmsRequest;
import com.vonage.smsjourney.adapter.in.web.dto.SmsResponse;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.domain.model.Sms;
import org.springframework.stereotype.Component;

@Component
public class SmsWebMapper {

    public static SendSmsCommand toSendSmsCommand(SmsRequest request) {
        String recipient = request.recipient();
        String message = request.message();
        CreateSmsJourneyCommand createSmsJourneyCommand = SmsJourneyWebMapper.toCreateSmsJourneyCommand(request.smsJourneyRequest());

        return new SendSmsCommand(recipient, message, createSmsJourneyCommand);
    }

    public SmsResponse toDto(Sms sms){
        return new SmsResponse(sms.getId(), sms.getStatus().name(), sms.getRecipient(), sms.getMessage(), sms.isDeleted(), sms.getCreatedAt());
    }
}
