package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.SolarWatchReportResults;
import com.codecool.solarwatch.service.SolarWatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolarWatchController.class)
class SolarWatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolarWatchService solarWatchService;

    @Test
    void testGetSunriseSunset_ValidRequest_ShouldReturn200() throws Exception {
        String city = "Budapest";
        String date = "2025-02-16";
        SolarWatchReportResults mockResponse = new SolarWatchReportResults("05:47:05 AM", "04:08:45 PM");

        Mockito.when(solarWatchService.getSunriseAndSunsetForByGivenParameters(city, LocalDate.parse(date)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/sunrise-sunset")
                        .param("city", city)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sunrise").value("05:47:05 AM"))
                .andExpect(jsonPath("$.sunset").value("04:08:45 PM"));
    }

    @Test
    void testGetSunriseSunset_InvalidDateFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/sunrise-sunset")
                        .param("city", "Budapest")
                        .param("date", "16-02-2025")) // Wrong format (DD-MM-YYYY)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid date format(YYYY-MM-DD)"));
    }

    @Test
    void testGetSunriseSunset_MissingDate_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/sunrise-sunset")
                        .param("city", "Budapest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSunriseSunset_DefaultCity_ShouldReturn200() throws Exception {
        String defaultCity = "Budapest";
        String date = "2025-02-16";
        SolarWatchReportResults mockResponse = new SolarWatchReportResults("05:47:05 AM", "04:08:45 PM");

        Mockito.when(solarWatchService.getSunriseAndSunsetForByGivenParameters(defaultCity, LocalDate.parse(date)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/sunrise-sunset")
                        .param("date", date)) // No city param -> uses default "Budapest"
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sunrise").value("05:47:05 AM"))
                .andExpect(jsonPath("$.sunset").value("04:08:45 PM"));
    }
}
