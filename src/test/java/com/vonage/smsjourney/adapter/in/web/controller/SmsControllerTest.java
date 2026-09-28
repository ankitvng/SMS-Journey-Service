package com.vonage.smsjourney.adapter.in.web.controller;

import com.vonage.smsjourney.adapter.in.web.controller.SmsController;
import com.vonage.smsjourney.adapter.in.web.dto.SmsJourneyRequest;
import com.vonage.smsjourney.adapter.in.web.dto.SmsRequest;
import com.vonage.smsjourney.adapter.in.web.mapper.SmsWebMapper;
import com.vonage.smsjourney.adapter.in.web.filter.RateLimiter;
import com.vonage.smsjourney.application.port.in.CreateSmsUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsUseCase;
import com.vonage.smsjourney.application.port.in.command.SendSmsCommand;
import com.vonage.smsjourney.domain.exception.SmsNotFoundException;
import com.vonage.smsjourney.domain.model.Sms;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsController.class)
@Import(SmsWebMapper.class)
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CreateSmsUseCase createSmsUseCase;
    @MockitoBean
    private GetSmsUseCase getSmsUseCase;
    @MockitoBean
    private DeleteSmsUseCase deleteSmsUseCase;
    @MockitoBean
    private RateLimiter rateLimiter;

    @Test
    void shouldCreateSms() throws Exception {
        SmsRequest request = new SmsRequest("+919876543210", "Hello",
                new SmsJourneyRequest("Summer Promo", "PRIMARY_ATTEMPT", "ROUTE_A", "ROUTE_B", 0.05));
        given(createSmsUseCase.create(any(SendSmsCommand.class))).willReturn(sms(1L, "+919876543210", "Hello", SmsStatus.CREATED));

        mockMvc.perform(post("/api/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recipient").value("+919876543210"))
                .andExpect(jsonPath("$.message").value("Hello"))
                .andExpect(jsonPath("$.smsStatus").value(SmsStatus.CREATED.name()));
    }

    @Test
    void shouldRejectInvalidSmsRequest() throws Exception {
        mockMvc.perform(post("/api/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"\",\"message\":\"\",\"smsJourneyRequest\":{\"campaignName\":\"campaign\",\"routingStep\":\"primary\",\"primaryRoute\":\"A\",\"fallbackRoute\":\"B\",\"cost\":0.05}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldReturnSms() throws Exception {
        given(getSmsUseCase.getById(1L)).willReturn(sms(1L, "+919876543210", "Hello", SmsStatus.SENT));

        mockMvc.perform(get("/api/sms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recipient").value("+919876543210"))
                .andExpect(jsonPath("$.message").value("Hello"))
                .andExpect(jsonPath("$.smsStatus").value(SmsStatus.SENT.name()));
    }

    @Test
    void shouldReturnNotFoundWhenSmsIsMissing() throws Exception {
        given(getSmsUseCase.getById(99L)).willThrow(new SmsNotFoundException(99L));

        mockMvc.perform(get("/api/sms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnAllSms() throws Exception {
        given(getSmsUseCase.getAllPaged(0, 20)).willReturn(List.of(
                sms(1L, "+919876543210", "Hello", SmsStatus.SENT),
                sms(2L, "+919876543211", "World", SmsStatus.SENT)));

        mockMvc.perform(get("/api/sms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].smsStatus").value(SmsStatus.SENT.name()))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].smsStatus").value(SmsStatus.SENT.name()));
    }

    @Test
    void shouldDeleteSms() throws Exception {
        mockMvc.perform(delete("/api/sms/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingSms() throws Exception {
        willThrow(new SmsNotFoundException(99L)).given(deleteSmsUseCase).delete(99L);

        mockMvc.perform(delete("/api/sms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private Sms sms(Long id, String recipient, String message, SmsStatus status) {
        return new Sms(id, recipient, message, status, false, LocalDateTime.now());
    }
}
