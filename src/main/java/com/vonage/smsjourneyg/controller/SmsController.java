package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    // Create SMS
    @PostMapping
    public ResponseEntity<SmsResponseDto> createSms(
            @Valid @RequestBody SmsRequestDto request) {

        SmsResponseDto response = smsService.createSms(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get SMS by ID
    @GetMapping("/{smsId}")
    public ResponseEntity<SmsResponseDto> getSms(
            @PathVariable Long smsId) {

        SmsResponseDto response = smsService.getSms(smsId);

        return ResponseEntity.ok(response);
    }

    // Get all SMS
    @GetMapping
    public ResponseEntity<List<SmsResponseDto>> getAllSms() {

        return ResponseEntity.ok(
                smsService.getAllSms()
        );
    }

    // Delete SMS
    @DeleteMapping("/{smsId}")
    public ResponseEntity<Void> deleteSms(
            @PathVariable Long smsId) {

        smsService.deleteSms(smsId);

        return ResponseEntity.noContent().build();
    }
}