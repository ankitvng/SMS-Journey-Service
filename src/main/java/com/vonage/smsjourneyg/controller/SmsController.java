package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.exception.ErrorResponse;
import com.vonage.smsjourneyg.rate.SmsRateLimiter;
import com.vonage.smsjourneyg.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    private final SmsRateLimiter smsRateLimiter;

    @PostMapping
    public ResponseEntity<?> createSms(@RequestBody SmsRequestDto request, HttpServletRequest httpRequest) {

        String clientId = getClientIp(httpRequest);

        if (!smsRateLimiter.isAllowed(clientId)) {

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorResponse(429, "Too Many Requests", "Too many requests. Please try again later.", LocalDateTime.now()));
        }

        SmsResponseDto response = smsService.createSms(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {

            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    @GetMapping("/{smsId}")
    public ResponseEntity<SmsResponseDto> getSms(@PathVariable Long smsId) {

        SmsResponseDto response = smsService.getSms(smsId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SmsResponseDto>> getAllSms(Pageable pageable) {

        return ResponseEntity.ok(smsService.getAllSms(pageable));
    }

    @DeleteMapping("/{smsId}")
    public ResponseEntity<Void> deleteSms(@PathVariable Long smsId) {

        smsService.deleteSms(smsId);

        return ResponseEntity.noContent().build();
    }
}
