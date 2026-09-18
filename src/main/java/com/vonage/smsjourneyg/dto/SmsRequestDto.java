package com.vonage.smsjourneyg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SmsRequestDto {

    @NotBlank(message = "Recipient is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String recipient;

    @NotBlank(message = "Message is required")
    @Size(
            max = 1600,
            message = "SMS message cannot exceed 1600 characters"
    )
    private String message;
}