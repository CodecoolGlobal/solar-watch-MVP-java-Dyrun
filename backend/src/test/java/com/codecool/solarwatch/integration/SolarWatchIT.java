package com.codecool.solarwatch.integration;

import com.codecool.solarwatch.model.dto.GeocodingReport;
import com.codecool.solarwatch.model.dto.SolarWatchReport;
import com.codecool.solarwatch.model.dto.SolarWatchReportResults;
import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.MemberRepository;
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
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=test",
        "spring.datasource.password=password",
        "spring.h2.console.enabled=true",
        "jwt.secret=1b3a3765492def88571af4a4ebb8b743b5ff18e9c2f55d12cdae8f52d51c5fde7705a8061e6c9039a935bd05f2a72dbcf0e08562c22eed68b11aeafb45c437a72c72eb00f8ced4e8616bd16a48beb0ca8aa523882746d370a42da2063950fb894be866037fd7228f5eb3bbf6938eba7f58ab55090db2a4cc4d3c1ce19d99fd902d8434e31f72d94c1084fa6a385dad23c575bb362375256e9fd9c52533c0d75e405b01f03e8cf7831be4ad60dc1bc994d8c96e535259caae6c3311d579ce3cde3e719c330aa1388c07b2a7c8cdeee5b99d97a2f8a347175db69b6d725fd16b6732beaf53c9752f5b54bfcf3e5745a8ab2d94d2c7da8cf47370358f924a6d9dab",
        "jwt.expiration.ms=86400000"
})
@AutoConfigureMockMvc
@Transactional
public class SolarWatchIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private SunriseSunsetRepository sunriseSunsetRepository;

    @Autowired
    private MemberRepository memberRepository;


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
        String loginRequest = String.format("""
        {
            "username": "%s",
            "password": "%s"
        }
        """, username, password);
        MvcResult result = mockMvc.perform(post("/api/user/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
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


        SolarWatchReportResults mockResults = new SolarWatchReportResults("5:00 AM", "8:00 PM");
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(new SolarWatchReport(mockResults)))
                .addHeader("Content-Type", "application/json"));


        mockMvc.perform(get("/api/sunrise-sunset")
                        .header("Authorization", "Bearer " + token)
                        .param("city", "Budapest")
                        .param("date", "2024-03-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sunrise").value("5:00 AM"))
                .andExpect(jsonPath("$.sunset").value("8:00 PM"));


        City savedCity = cityRepository.findByName("Budapest").orElseThrow();
        assertThat(savedCity.getLatitude()).isEqualTo(47.4979);

        SunriseSunset savedSS = sunriseSunsetRepository.findByCityAndDate(savedCity, LocalDate.parse("2024-03-25"))
                .orElseThrow();
        assertThat(savedSS.getSunrise()).isEqualTo("5:00 AM");
    }

    @Test
    void createCityWithAdminUserResultIsSuccess() throws Exception {
        String token = getUserToken("admin", "admin");
        City city = new City();
        city.setName("Berlin");
        city.setLatitude(52.5200);
        city.setLongitude(13.4050);
        city.setState("Berlin");
        city.setCountry("Germany");

        mockMvc.perform(post("/api/city")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(city)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Berlin"))
                .andExpect(jsonPath("$.country").value("Germany"));
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

        mockMvc.perform(get("/api/sunrise-sunset/")
                        .header("Authorization", "Bearer " + token)
                        .param("city", "Budapest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sunrise").value("6:00 AM"))
                .andExpect(jsonPath("$[0].date").value("2024-03-25"));
    }

    @Test
    void deleteCityWithAdminUserResultIsSuccess() throws Exception {
        String token = getUserToken("admin", "admin");
        City city = createTestCity();
        cityRepository.save(city);

        mockMvc.perform(delete("/api/city/{id}", city.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertThat(cityRepository.findById(city.getId())).isEmpty();
    }

    @Test
    void registerAndLoginWithValidUserResultIsSuccess() throws Exception {
        UserRequest userRequest = new UserRequest(
                "newUser",
                "test"
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().is(201));

        mockMvc.perform(post("/api/user/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("newUser"));
        assertThat(memberRepository.findByName("newUser")).isNotEmpty();
    }
}