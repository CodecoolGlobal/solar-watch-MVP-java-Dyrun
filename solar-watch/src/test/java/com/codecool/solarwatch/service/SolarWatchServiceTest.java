package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.GeocodingReport;
import com.codecool.solarwatch.model.SolarWatchReport;
import com.codecool.solarwatch.model.SolarWatchReportResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolarWatchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SolarWatchService solarWatchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSunriseAndSunsetForValidCityAndDate() {
        // Mock geocoding response
        GeocodingReport[] geocodingResponse = {new GeocodingReport(47.4979, 19.0402)};
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenReturn(geocodingResponse);

        // Mock sunrise-sunset response
        SolarWatchReportResults results = new SolarWatchReportResults("05:47:05 AM", "04:08:45 PM");
        SolarWatchReport mockResponse = new SolarWatchReport(results);
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(mockResponse);

        // Call the service
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetForByGivenParameters("Budapest", LocalDate.of(2025, 2, 16));

        // Assertions
        assertNotNull(result);
        assertEquals("05:47:05 AM", result.sunrise());
        assertEquals("04:08:45 PM", result.sunset());

        // Verify the external API calls were made
        verify(restTemplate, times(1)).getForObject(contains("geo/1.0/direct"), eq(GeocodingReport[].class));
        verify(restTemplate, times(1)).getForObject(contains("sunrise-sunset.org"), eq(SolarWatchReport.class));
    }


    @Test
    void testGetSunriseAndSunset_APIError_ShouldThrowException() {
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenThrow(new RuntimeException("API request failed"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetForByGivenParameters("Budapest", LocalDate.of(2025, 2, 16)));

        assertEquals("API request failed", exception.getMessage());
    }
}
