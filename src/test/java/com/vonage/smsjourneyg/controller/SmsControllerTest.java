package com.vonage.smsjourneyg.controller;

import com.vonage.smsjourneyg.config.CacheConfig;
import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.exception.SmsNotFoundException;
import com.vonage.smsjourneyg.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SmsController.class)
@Import(CacheConfig.class)
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SmsService smsService;

    @Test
    void shouldCreateSms() throws Exception {
        SmsRequestDto request = new SmsRequestDto();
        request.setRecipient("+919876543210");
        request.setMessage("Hello");

        SmsResponseDto response = new SmsResponseDto();
        response.setSmsId(1L);
        response.setRecipient("+919876543210");
        response.setMessage("Hello");
        response.setStatus(SmsStatus.CREATED);

        when(smsService.createSms(any(SmsRequestDto.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.smsId").value(1))
                .andExpect(jsonPath("$.recipient").value("+919876543210"))
                .andExpect(jsonPath("$.message").value("Hello"))
                .andExpect(jsonPath("$.status").value(SmsStatus.CREATED.name()));
    }

    @Test
    void shouldRejectInvalidSmsRequest() throws Exception {
        SmsRequestDto request = new SmsRequestDto();
        request.setRecipient("");
        request.setMessage("");

        mockMvc.perform(
                        post("/api/sms")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.recipient").exists())
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    void shouldReturnSms() throws Exception {
        SmsResponseDto response = new SmsResponseDto();
        response.setSmsId(1L);
        response.setRecipient("+919876543210");
        response.setMessage("Hello");
        response.setStatus(SmsStatus.SENT);

        when(smsService.getSms(1L)).thenReturn(response);

        mockMvc.perform(
                        get("/api/sms/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smsId").value(1))
                .andExpect(jsonPath("$.recipient").value("+919876543210"))
                .andExpect(jsonPath("$.message").value("Hello"))
                .andExpect(jsonPath("$.status").value(SmsStatus.SENT.name()));
    }

    @Test
    void shouldReturnNotFoundWhenSmsIsMissing() throws Exception {
        when(smsService.getSms(99L)).thenThrow(new SmsNotFoundException(99L));

        mockMvc.perform(get("/api/sms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnAllSms() throws Exception {
        SmsResponseDto response1 = new SmsResponseDto();
        response1.setSmsId(1L);
        response1.setRecipient("+919876543210");
        response1.setMessage("Hello");
        response1.setStatus(SmsStatus.SENT);

        SmsResponseDto response2 = new SmsResponseDto();
        response2.setSmsId(2L);
        response2.setRecipient("+919876543211");
        response2.setMessage("World");
        response2.setStatus(SmsStatus.SENT);

        when(smsService.getAllSms(any())).thenReturn(new PageImpl<>(List.of(response1, response2)));

        mockMvc.perform(
                        get("/api/sms")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].smsId").value(1))
                .andExpect(jsonPath("$.content[0].status").value(SmsStatus.SENT.name()))
                .andExpect(jsonPath("$.content[1].smsId").value(2))
                .andExpect(jsonPath("$.content[1].status").value(SmsStatus.SENT.name()));
    }

    @Test
    void shouldDeleteSms() throws Exception {
        doNothing().when(smsService).deleteSms(1L);

        mockMvc.perform(
                        delete("/api/sms/1")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingSms() throws Exception {
        doThrow(new SmsNotFoundException(99L)).when(smsService).deleteSms(99L);

        mockMvc.perform(delete("/api/sms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
