package com.codecool.solarwatch.integration;

import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.repository.CityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@Transactional
public class CityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    private static MockWebServer mockWebServer;

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Berlin"))
                .andExpect(jsonPath("$.country").value("Germany"));
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
}