package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.entity.SmsJourney;
import com.vonage.smsjourneyg.repository.SmsJourneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsJourneyService {

    @Autowired
    private SmsJourneyRepository repository;

    public SmsJourney createJourney(SmsJourney journey) {
        calculateMetrics(journey);
        return repository.save(journey);
    }

    public List<SmsJourney> getAllJourneys() {
        return repository.findAll();
    }

    public SmsJourney getJourneyById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Journey not found"));
    }

    public SmsJourney updateJourney(Long id, SmsJourney updatedData) {
        SmsJourney existing = getJourneyById(id);

        if ("Sent".equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot modify an already sent journey");
        }

        if (updatedData.getSmsBody() != null) {
            existing.setSmsBody(updatedData.getSmsBody());
            calculateMetrics(existing); // Recalculate cost automatically if text changes
        }
        if (updatedData.getFallbackRoute() != null) {
            existing.setFallbackRoute(updatedData.getFallbackRoute());
        }

        return repository.save(existing);
    }

    public void deleteJourney(Long id) {
        repository.deleteById(id);
    }

    // Smart Character & Pricing Calculation Engine
    private void calculateMetrics(SmsJourney journey) {
        String body = journey.getSmsBody();
        int charCount = body.length();
        journey.setCharCount(charCount);

        // Basic check for Emojis/Unicode characters
        boolean isUnicode = !body.matches("^[\\u0000-\\u007F]*$");
        journey.setUnicode(isUnicode);

        // Enforce carrier segment constraints
        int limitPerSegment = isUnicode ? 70 : 160;
        int segments = (int) Math.ceil((double) charCount / limitPerSegment);
        journey.setSegments(Math.max(segments, 1));

        // Router pricing decisions
        double rate = 0.0050; // Default route cost
        if ("Twilio".equalsIgnoreCase(journey.getPrimaryRoute())) rate = 0.0075;
        else if ("Infobip".equalsIgnoreCase(journey.getPrimaryRoute())) rate = 0.0090;

        journey.setCost(journey.getSegments() * rate);
    }
}
