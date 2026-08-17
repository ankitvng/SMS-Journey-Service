package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.service.SmsJourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsJourneyController {

    private final SmsJourneyService journeyService;


    @PostMapping("/{smsId}/journeys")
    public ResponseEntity<SmsJourneyDto> createJourney(
            @PathVariable Long smsId,
            @RequestBody SmsJourneyDto request) {

        SmsJourneyDto response =
                journeyService.createJourney(
                        smsId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{smsId}/journeys")
    public ResponseEntity<List<SmsJourneyDto>> getJourneys(
            @PathVariable Long smsId) {

        return ResponseEntity.ok(
                journeyService.getJourneysBySms(smsId)
        );
    }

    @PostMapping("/journeys/{journeyId}/process")
    public ResponseEntity<Void> processJourney(
            @PathVariable Long journeyId) {

        journeyService.processJourney(journeyId);

        return ResponseEntity.ok().build();
    }
}