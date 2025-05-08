package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.dto.GeocodingReport;
import com.codecool.solarwatch.model.dto.SolarWatchReport;
import com.codecool.solarwatch.model.dto.SolarWatchReportResults;
import com.codecool.solarwatch.model.dto.SunriseSunsetResponse;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.SunriseSunsetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolarWatchServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private SunriseSunsetRepository sunriseSunsetRepository;

    @InjectMocks
    private SolarWatchService solarWatchService;

    @Value("${api.sunrise-sunset.url}")
    static String sunriseSunsetUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.sunrise-sunset.url", () -> sunriseSunsetUrl);
    }

    private final LocalDate testDate = LocalDate.of(2024, 3, 25);

    private City createTestCity() {
        City city = new City();
        city.setName("London");
        city.setLatitude(51.5074);
        city.setLongitude(-0.1278);
        city.setState("England");
        city.setCountry("UK");
        return city;
    }

    private SunriseSunset createTestSunriseSunset(City city) {
        SunriseSunset ss = new SunriseSunset();
        ss.setId(1L);
        ss.setCity(city);
        ss.setDate(testDate);
        ss.setSunrise("06:00:00 AM");
        ss.setSunset("06:00:00 PM");
        return ss;
    }

    @Test
    void getSunriseAndSunsetWhenExistingDataItReturnsFromDatabase() {
        City city = createTestCity();
        SunriseSunset ss = createTestSunriseSunset(city);
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.of(ss));
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate);
        assertAll(
                () -> assertEquals("06:00:00 AM", result.sunrise()),
                () -> assertEquals("06:00:00 PM", result.sunset())
        );
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getSunriseAndSunsetWhenNewCityAndNewDataItSavesBothEntities() {
        GeocodingReport geoResponse = new GeocodingReport(
                51.5074,
                -0.1278,
                "City of London",
                "United Kingdom",
                "England"
        );
        SolarWatchReport solarResponse = new SolarWatchReport(
                new SolarWatchReportResults("07:15:00 AM", "07:45:00 PM")
        );
        when(cityRepository.findByName("London")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(contains("geo/1.0/direct"), eq(GeocodingReport[].class)))
                .thenReturn(new GeocodingReport[]{geoResponse});
        String expectedSunriseSunsetUrl = String.format(
                "%s/json?lat=51.5074&lng=-0.1278&date=%s", sunriseSunsetUrl, testDate
        );

        when(restTemplate.getForObject(eq(expectedSunriseSunsetUrl), eq(SolarWatchReport.class)))
                .thenReturn(solarResponse);
        ArgumentCaptor<City> cityCaptor = ArgumentCaptor.forClass(City.class);
        ArgumentCaptor<SunriseSunset> ssCaptor = ArgumentCaptor.forClass(SunriseSunset.class);
        when(cityRepository.save(cityCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(sunriseSunsetRepository.save(ssCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate);
        City savedCity = cityCaptor.getValue();
        assertAll(
                () -> assertEquals("London", savedCity.getName()),
                () -> assertEquals(51.5074, savedCity.getLatitude()),
                () -> assertEquals("United Kingdom", savedCity.getCountry())
        );
        SunriseSunset savedSS = ssCaptor.getValue();
        assertAll(
                () -> assertEquals(savedCity, savedSS.getCity()),
                () -> assertEquals(testDate, savedSS.getDate()),
                () -> assertEquals("07:15:00 AM", savedSS.getSunrise())
        );
        assertEquals("07:15:00 AM", result.sunrise());
    }

    @Test
    void getSunriseSunsetByCityWhenReturnsProperResponses() {
        City city = createTestCity();
        SunriseSunset ss = createTestSunriseSunset(city);
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCity(city)).thenReturn(List.of(ss));
        List<SunriseSunsetResponse> result = solarWatchService.getSunriseSunsetByCity("London");
        assertEquals(1, result.size());
        SunriseSunsetResponse response = result.get(0);
        assertAll(
                () -> assertEquals(ss.getSunrise(), response.sunrise()),
                () -> assertEquals(ss.getSunset(), response.sunset()),
                () -> assertEquals(ss.getDate(), response.date())
        );
    }

    @Test
    void updateSunriseSunsetWhenValidIdItUpdatesTimes() {
        SunriseSunset existing = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sunriseSunsetRepository.save(existing)).thenReturn(existing);
        SunriseSunset updated = solarWatchService.updateSunriseSunset(1L, "07:00:00 AM", "07:30:00 PM");
        assertAll(
                () -> assertEquals("07:00:00 AM", updated.getSunrise()),
                () -> assertEquals("07:30:00 PM", updated.getSunset()),
                () -> assertEquals(testDate, updated.getDate()),
                () -> verify(sunriseSunsetRepository).save(existing)
        );
    }

    @Test
    void deleteSunriseSunsetWhenExistingIdItDeletesRecord() {
        when(sunriseSunsetRepository.existsById(1L)).thenReturn(true);
        solarWatchService.deleteSunriseSunset(1L);
        verify(sunriseSunsetRepository).deleteById(1L);
    }

    @Test
    void getAllSunriseSunsetWhenReturnsAllRecords() {
        SunriseSunset ss1 = createTestSunriseSunset(createTestCity());
        SunriseSunset ss2 = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.findAll()).thenReturn(List.of(ss1, ss2));
        List<SunriseSunset> result = solarWatchService.getAllSunriseSunset();
        assertEquals(2, result.size());
    }

    @Test
    void getSunriseAndSunsetWhenInvalidCityItThrowsException() {
        when(cityRepository.findByName("InvalidCity")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenReturn(new GeocodingReport[0]);
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("InvalidCity", testDate)
        );
    }

    @Test
    void getSunriseAndSunsetWhenInvalidSolarDataItThrowsException() {
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void deleteSunriseSunsetWhenNonExistingIdItThrowsException() {
        when(sunriseSunsetRepository.existsById(99L)).thenReturn(false);
        assertThrows(RuntimeException.class, () ->
                solarWatchService.deleteSunriseSunset(99L)
        );
        verify(sunriseSunsetRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateSunriseSunsetWhenNonExistingIdItThrowsException() {
        when(sunriseSunsetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                solarWatchService.updateSunriseSunset(99L, "07:00:00 AM", "07:30:00 PM")
        );
        verify(sunriseSunsetRepository, never()).save(any());
    }

    @Test
    void getSunriseSunsetByIdWhenNonExistingIdItReturnsEmpty() {
        when(sunriseSunsetRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<SunriseSunset> result = solarWatchService.getSunriseSunsetById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveSunriseSunsetWhenValidEntityItReturnsSavedEntity() {
        SunriseSunset newRecord = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.save(newRecord)).thenReturn(newRecord);
        SunriseSunset result = solarWatchService.saveSunriseSunset(newRecord);
        assertNotNull(result);
        assertEquals(newRecord.getSunrise(), result.getSunrise());
        verify(sunriseSunsetRepository).save(newRecord);
    }

    @Test
    void getSunriseAndSunsetWhenGeocodingApiReturnsNullItThrowsException() {
        when(cityRepository.findByName("London")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void getSunriseAndSunsetWhenSolarApiReturnsNullResultsItThrowsException() {
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(new SolarWatchReport(null));
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void getSunriseAndSunsetWhenSolarApiReturnsNullItThrowsException() {
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

}
