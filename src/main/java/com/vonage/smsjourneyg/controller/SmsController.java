package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping
    public ResponseEntity<SmsResponseDto> createSms(
            @Valid @RequestBody SmsRequestDto request) {

        SmsResponseDto response = smsService.createSms(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{smsId}")
    public ResponseEntity<SmsResponseDto> getSms(
            @PathVariable Long smsId) {

        SmsResponseDto response = smsService.getSms(smsId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SmsResponseDto>> getAllSms(
            Pageable pageable) {

        return ResponseEntity.ok(
                smsService.getAllSms(pageable)
        );
    }

    @DeleteMapping("/{smsId}")
    public ResponseEntity<Void> deleteSms(
            @PathVariable Long smsId) {

        smsService.deleteSms(smsId);

        return ResponseEntity.noContent().build();
    }
}