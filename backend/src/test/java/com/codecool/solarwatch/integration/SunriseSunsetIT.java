package com.codecool.solarwatch.integration;

import com.codecool.solarwatch.model.dto.GeocodingReport;
import com.codecool.solarwatch.model.dto.SunriseSunsetReport;
import com.codecool.solarwatch.model.dto.SunriseSunsetReportResults;
import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.SunriseSunsetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@Transactional
public class SunriseSunsetIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private SunriseSunsetRepository sunriseSunsetRepository;

    private static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("api.openweathermap.url", () -> "http://localhost:" + mockWebServer.getPort());
        registry.add("api.sunrise-sunset.url", () -> "http://localhost:" + mockWebServer.getPort());
    }

    @BeforeAll
    static void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void resetMocks() {
        mockWebServer.url("/").toString();
    }

    private City createTestCity() {
        City city = new City();
        city.setName("Budapest");
        city.setLatitude(47.4979);
        city.setLongitude(19.0402);
        city.setState("Pest");
        city.setCountry("HU");
        return city;
    }

    private GeocodingReport createTestGeocodingReport() {
        City city = createTestCity();
        return new GeocodingReport(
                city.getLatitude(),
                city.getLongitude(),
                city.getName(),
                city.getCountry(),
                city.getState()
        );
    }

    private SunriseSunset createTestSunriseSunset(City city) {
        SunriseSunset ss = new SunriseSunset();
        ss.setCity(city);
        ss.setDate(LocalDate.parse("2024-03-25"));
        ss.setSunrise("6:00 AM");
        ss.setSunset("6:00 PM");
        return ss;
    }

    private String getUserToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("jwt").asText();
    }

    @Test
    void getSunriseSunsetWithNewCityThenReturnsAndStoresData() throws Exception {
        String token = getUserToken("admin", "admin");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(List.of(createTestGeocodingReport())))
                .addHeader("Content-Type", "application/json"));

        SunriseSunsetReportResults mockResults = new SunriseSunsetReportResults("5:00 AM", "8:00 PM");
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(new SunriseSunsetReport(mockResults)))
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/sunrise-sunset")
                        .header("Authorization", "Bearer " + token)
                        .param("city", "Budapest")
                        .param("date", "2024-03-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sunrise").value("5:00 AM"))
                .andExpect(jsonPath("$.sunset").value("8:00 PM"))
                .andExpect(jsonPath("$.cityName").value("Budapest"))
                .andExpect(jsonPath("$.date").value("2024-03-25"));

        City savedCity = cityRepository.findByName("Budapest").orElseThrow();
        assertThat(savedCity.getLatitude()).isEqualTo(47.4979);

        SunriseSunset savedSS = sunriseSunsetRepository.findByCityAndDate(savedCity, LocalDate.parse("2024-03-25"))
                .orElseThrow();
        assertThat(savedSS.getSunrise()).isEqualTo("5:00 AM");
    }

    @Test
    void updateSunriseSunsetWithValidIdResultIsSuccess() throws Exception {
        String token = getUserToken("admin", "admin");
        City city = createTestCity();
        cityRepository.save(city);
        SunriseSunset ss = createTestSunriseSunset(city);
        sunriseSunsetRepository.save(ss);

        mockMvc.perform(put("/api/sunrise-sunset/{id}", ss.getId())
                        .header("Authorization", "Bearer " + token)
                        .param("sunrise", "7:00 AM")
                        .param("sunset", "7:00 PM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sunrise").value("7:00 AM"))
                .andExpect(jsonPath("$.sunset").value("7:00 PM"));

        SunriseSunset updated = sunriseSunsetRepository.findById(ss.getId()).orElseThrow();
        assertThat(updated.getSunrise()).isEqualTo("7:00 AM");
    }

    @Test
    void getSunriseSunsetByCityThenReturnsData() throws Exception {
        String token = getUserToken("admin", "admin");
        City city = createTestCity();
        cityRepository.save(city);
        SunriseSunset ss = createTestSunriseSunset(city);
        sunriseSunsetRepository.save(ss);

        mockMvc.perform(get("/api/sunrise-sunset/city")
                        .header("Authorization", "Bearer " + token)
                        .param("cityName", "Budapest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sunrise").value("6:00 AM"))
                .andExpect(jsonPath("$[0].date").value("2024-03-25"));
    }
}