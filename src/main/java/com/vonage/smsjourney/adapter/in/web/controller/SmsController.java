package com.vonage.smsjourney.adapter.in.web.controller;

import com.vonage.smsjourney.adapter.in.web.dto.SmsRequest;
import com.vonage.smsjourney.adapter.in.web.dto.SmsResponse;
import com.vonage.smsjourney.adapter.in.web.mapper.SmsJourneyWebMapper;
import com.vonage.smsjourney.adapter.in.web.mapper.SmsWebMapper;
import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.application.port.in.CreateSmsUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsUseCase;
import com.vonage.smsjourney.domain.model.Sms;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final CreateSmsUseCase createSmsUseCase;
    private final GetSmsUseCase getSmsUseCase;
    private final DeleteSmsUseCase deleteSmsUseCase;
    private final SmsWebMapper smsWebMapper;

    @PostMapping
    public ResponseEntity<SmsResponse> create(@Valid @RequestBody SmsRequest request) {

        Sms result = createSmsUseCase.create(new SendSmsCommand(request.recipient(), request.message(), SmsJourneyWebMapper.toCreateSmsJourneyCommand(request.smsJourneyRequest())));

        return ResponseEntity.status(HttpStatus.CREATED).body(SmsResponse.from(result));
    }

    @GetMapping("/{smsId}")
    public ResponseEntity<SmsResponse> getSms(@PathVariable Long smsId) {
        Sms result = getSmsUseCase.getById(smsId);
        return ResponseEntity.ok(SmsResponse.from(result));
    }

    @GetMapping
    public Page<SmsResponse> getAllSms(Pageable pageable) {
        List<Sms> smsList = getSmsUseCase.getAllPaged(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        List<SmsResponse> response = smsList.stream()
                .map(smsWebMapper::toDto)
                .toList();

        return new PageImpl<>(response, pageable, response.size());
    }

    @DeleteMapping("/{smsId}")
    public ResponseEntity<Void> deleteSms(@PathVariable Long smsId) {

        deleteSmsUseCase.delete(smsId);

        return ResponseEntity.noContent().build();
    }

}



