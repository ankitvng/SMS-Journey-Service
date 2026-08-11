package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.service.SmsJourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@RequestMapping("/api/journeys")
public class SmsJourneyController {
    @Autowired
    private SmsJourneyService service;

    @PostMapping
    public ResponseEntity<SmsJourney> create(@RequestBody SmsJourney journey) {
        return ResponseEntity.ok(service.createJourney(journey));
    }

    @GetMapping
    public ResponseEntity<List<SmsJourney>> getAll() {
        return ResponseEntity.ok(service.getAllJourneys());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmsJourney> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getJourneyById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SmsJourney> update(@PathVariable Long id, @RequestBody SmsJourney updatedData) {
        return ResponseEntity.ok(service.updateJourney(id, updatedData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteJourney(id);
        return ResponseEntity.ok("Journey deleted successfully.");
    }
}
