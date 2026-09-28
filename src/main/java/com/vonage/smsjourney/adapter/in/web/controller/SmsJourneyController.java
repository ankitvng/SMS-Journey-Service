package com.vonage.smsjourney.adapter.in.web.controller;

import com.vonage.smsjourney.adapter.in.web.dto.SmsJourneyRequest;
import com.vonage.smsjourney.adapter.in.web.dto.SmsJourneyResponse;
import com.vonage.smsjourney.adapter.in.web.mapper.SmsJourneyWebMapper;
import com.vonage.smsjourney.application.port.in.CreateSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsJourneyUseCase;
import com.vonage.smsjourney.domain.model.SmsJourney;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsJourneyController  {

    private final CreateSmsJourneyUseCase createSmsJourneyUseCase;
    private final GetSmsJourneyUseCase getSmsJourneyUseCase;
    private final DeleteSmsJourneyUseCase deleteSmsJourneyUseCase;


    @PostMapping("/{smsId}/journeys")
    public ResponseEntity<SmsJourneyResponse> createJourney(@Valid @PathVariable Long smsId, @Valid @RequestBody SmsJourneyRequest request) {

        SmsJourney response = createSmsJourneyUseCase.createSmsJourney(smsId, SmsJourneyWebMapper.toCreateSmsJourneyCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(SmsJourneyResponse.from(response));
    }


    @GetMapping("/{smsId}/journeys")
    public ResponseEntity<List<SmsJourneyResponse>> getJourneys(@PathVariable Long smsId) {
        List<SmsJourney> journeys = getSmsJourneyUseCase.getById(smsId);

        return ResponseEntity.ok(journeys.stream().map(SmsJourneyResponse::from).collect(Collectors.toList()));
    }

    @DeleteMapping("/journeys/{journeyId}")
    public ResponseEntity<Void> deleteJourney(@PathVariable Long journeyId) {
        deleteSmsJourneyUseCase.delete(journeyId);
        return ResponseEntity.noContent().build();
    }
}
