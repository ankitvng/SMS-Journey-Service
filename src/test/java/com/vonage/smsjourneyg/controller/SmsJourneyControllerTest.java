package com.vonage.smsjourneyg.controller;


import com.vonage.smsjourneyg.dto.SmsJourneyDto;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.service.SmsJourneyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
    private SmsJourneyService journeyService;

    @Test
    void shouldCreateJourney() throws Exception {
        Long smsId = 100L;

        SmsJourneyDto request = new SmsJourneyDto();
        request.setCampaignName("Summer Promo");
        request.setRoutingStep("PRIMARY_ATTEMPT");
        request.setPrimaryRoute("ROUTE_A");
        request.setFallbackRoute("ROUTE_B");
        request.setCost(0.05);
        request.setStatus(SmsStatus.PENDING);

        SmsJourneyDto response = new SmsJourneyDto();
        response.setId(1L);
        response.setCampaignName("Summer Promo");
        response.setRoutingStep("PRIMARY_ATTEMPT");
        response.setPrimaryRoute("ROUTE_A");
        response.setFallbackRoute("ROUTE_B");
        response.setCost(0.05);
        response.setStatus(SmsStatus.PENDING);

        when(journeyService.createJourney(eq(smsId), any(SmsJourneyDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/sms/{smsId}/journeys", smsId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.campaignName").value("Summer Promo"))
                .andExpect(jsonPath("$.routingStep").value("PRIMARY_ATTEMPT"))
                .andExpect(jsonPath("$.primaryRoute").value("ROUTE_A"))
                .andExpect(jsonPath("$.fallbackRoute").value("ROUTE_B"))
                .andExpect(jsonPath("$.cost").value(0.05))
                .andExpect(jsonPath("$.status").value(SmsStatus.PENDING.name()));
    }

    @Test
    void shouldGetJourneysBySms() throws Exception {
        Long smsId = 100L;

        SmsJourneyDto journey1 = new SmsJourneyDto();
        journey1.setId(1L);
        journey1.setCampaignName("Campaign 1");
        journey1.setStatus(SmsStatus.COMPLETED);

        SmsJourneyDto journey2 = new SmsJourneyDto();
        journey2.setId(2L);
        journey2.setCampaignName("Campaign 2");
        journey2.setStatus(SmsStatus.PENDING);

        when(journeyService.getJourneysBySms(smsId))
                .thenReturn(List.of(journey1, journey2));

        mockMvc.perform(
                        get("/api/sms/{smsId}/journeys", smsId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].campaignName").value("Campaign 1"))
                .andExpect(jsonPath("$[0].status").value(SmsStatus.COMPLETED.name()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].campaignName").value("Campaign 2"))
                .andExpect(jsonPath("$[1].status").value(SmsStatus.PENDING.name()));
    }

    @Test
    void shouldProcessJourney() throws Exception {
        Long journeyId = 1L;

        doNothing().when(journeyService).processJourney(journeyId);

        mockMvc.perform(
                        post("/api/sms/journeys/{journeyId}/process", journeyId)
                )
                .andExpect(status().isOk());
    }
}