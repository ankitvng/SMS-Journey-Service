package com.vonage.smsjourneyg.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
}
