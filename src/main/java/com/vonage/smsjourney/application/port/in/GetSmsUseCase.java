package com.vonage.smsjourney.application.port.in;

import com.vonage.smsjourney.domain.model.Sms;

import java.util.List;

public interface GetSmsUseCase {
    Sms getById(Long smsId);

    List<Sms> getAllPaged(int page, int size);
}
