package com.vonage.smsjourneyg.controller;


import com.vonage.smsjourneyg.dto.SmsRequestDto;
import com.vonage.smsjourneyg.dto.SmsResponseDto;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SmsController.class)
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

        when(smsService.getAllSms()).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                        get("/api/sms")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].smsId").value(1))
                .andExpect(jsonPath("$[0].status").value(SmsStatus.SENT.name()))
                .andExpect(jsonPath("$[1].smsId").value(2))
                .andExpect(jsonPath("$[1].status").value(SmsStatus.SENT.name()));
    }

    @Test
    void shouldDeleteSms() throws Exception {
        doNothing().when(smsService).deleteSms(1L);

        mockMvc.perform(
                        delete("/api/sms/1")
                )
                .andExpect(status().isNoContent());
    }
}
