package com.vonage.smsjourney.adapter.in.web.controller;

import com.vonage.smsjourney.adapter.in.web.controller.SmsJourneyController;
import com.vonage.smsjourney.adapter.in.web.dto.SmsJourneyRequest;
import com.vonage.smsjourney.adapter.in.web.filter.RateLimiter;
import com.vonage.smsjourney.application.port.in.CreateSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.DeleteSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.GetSmsJourneyUseCase;
import com.vonage.smsjourney.application.port.in.command.CreateSmsJourneyCommand;
import com.vonage.smsjourney.domain.model.SmsJourney;
import com.vonage.smsjourney.domain.model.SmsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsJourneyController.class)
class SmsJourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CreateSmsJourneyUseCase createSmsJourneyUseCase;
    @MockitoBean
    private GetSmsJourneyUseCase getSmsJourneyUseCase;
    @MockitoBean
    private DeleteSmsJourneyUseCase deleteSmsJourneyUseCase;
    @MockitoBean
    private RateLimiter rateLimiter;

    @Test
    void shouldCreateJourney() throws Exception {
        SmsJourneyRequest request = new SmsJourneyRequest("Summer Promo", "PRIMARY_ATTEMPT",
                "ROUTE_A", "ROUTE_B", 0.05);
        SmsJourney response = journey(1L, "Summer Promo", "PRIMARY_ATTEMPT", "ROUTE_A", "ROUTE_B", 0.05,
                SmsStatus.SCHEDULED);
        given(createSmsJourneyUseCase.createSmsJourney(eq(100L), any(CreateSmsJourneyCommand.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/sms/{smsId}/journeys", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.campaignName").value("Summer Promo"))
                .andExpect(jsonPath("$.routingStep").value("PRIMARY_ATTEMPT"))
                .andExpect(jsonPath("$.primaryRoute").value("ROUTE_A"))
                .andExpect(jsonPath("$.fallbackRoute").value("ROUTE_B"))
                .andExpect(jsonPath("$.cost").value(0.05))
                .andExpect(jsonPath("$.status").value(SmsStatus.SCHEDULED.name()));
    }

    @Test
    void shouldGetJourneysBySms() throws Exception {
        given(getSmsJourneyUseCase.getById(100L)).willReturn(List.of(
                journey(1L, "Campaign 1", null, null, null, 0, SmsStatus.DELIVERED),
                journey(2L, "Campaign 2", null, null, null, 0, SmsStatus.SCHEDULED)));

        mockMvc.perform(get("/api/sms/{smsId}/journeys", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].campaignName").value("Campaign 1"))
                .andExpect(jsonPath("$[0].status").value(SmsStatus.DELIVERED.name()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].campaignName").value("Campaign 2"))
                .andExpect(jsonPath("$[1].status").value(SmsStatus.SCHEDULED.name()));
    }

    @Test
    void shouldReturnEmptyListWhenNoJourneysExist() throws Exception {
        given(getSmsJourneyUseCase.getById(100L)).willReturn(List.of());

        mockMvc.perform(get("/api/sms/{smsId}/journeys", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private SmsJourney journey(Long id, String name, String step, String primary, String fallback,
                               double cost, SmsStatus status) {
        return new SmsJourney(id, name, step, primary, fallback, cost, status, null, null);
    }
}
