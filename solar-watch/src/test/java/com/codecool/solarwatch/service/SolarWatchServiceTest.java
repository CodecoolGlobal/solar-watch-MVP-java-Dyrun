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
    void getSunriseAndSunset_ExistingData_ReturnsFromDatabase() {
        // Arrange
        City city = createTestCity();
        SunriseSunset ss = createTestSunriseSunset(city);

        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.of(ss));

        // Act
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate);

        // Assert
        assertAll(
                () -> assertEquals("06:00:00 AM", result.sunrise()),
                () -> assertEquals("06:00:00 PM", result.sunset())
        );
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getSunriseAndSunset_NewCityAndNewData_SavesBothEntities() {
        // Arrange
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
        when(restTemplate.getForObject(contains("sunrise-sunset.org"), eq(SolarWatchReport.class)))
                .thenReturn(solarResponse);

        ArgumentCaptor<City> cityCaptor = ArgumentCaptor.forClass(City.class);
        ArgumentCaptor<SunriseSunset> ssCaptor = ArgumentCaptor.forClass(SunriseSunset.class);

        when(cityRepository.save(cityCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(sunriseSunsetRepository.save(ssCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate);

        // Assert
        // Verify city creation
        City savedCity = cityCaptor.getValue();
        assertAll(
                () -> assertEquals("London", savedCity.getName()),
                () -> assertEquals(51.5074, savedCity.getLatitude()),
                () -> assertEquals("United Kingdom", savedCity.getCountry())
        );

        // Verify sunrise/sunset creation
        SunriseSunset savedSS = ssCaptor.getValue();
        assertAll(
                () -> assertEquals(savedCity, savedSS.getCity()),
                () -> assertEquals(testDate, savedSS.getDate()),
                () -> assertEquals("07:15:00 AM", savedSS.getSunrise())
        );

        // Verify final result
        assertEquals("07:15:00 AM", result.sunrise());
    }

    @Test
    void getSunriseSunsetByCity_ReturnsProperResponses() {
        // Arrange
        City city = createTestCity();
        SunriseSunset ss = createTestSunriseSunset(city);
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCity(city)).thenReturn(List.of(ss));

        // Act
        List<SunriseSunsetResponse> result = solarWatchService.getSunriseSunsetByCity("London");

        // Assert
        assertEquals(1, result.size());
        SunriseSunsetResponse response = result.get(0);
        assertAll(
                () -> assertEquals(ss.getSunrise(), response.sunrise()),
                () -> assertEquals(ss.getSunset(), response.sunset()),
                () -> assertEquals(ss.getDate(), response.date())
        );
    }

    @Test
    void updateSunriseSunset_ValidId_UpdatesTimes() {
        // Arrange
        SunriseSunset existing = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sunriseSunsetRepository.save(existing)).thenReturn(existing);

        // Act
        SunriseSunset updated = solarWatchService.updateSunriseSunset(1L, "07:00:00 AM", "07:30:00 PM");

        // Assert
        assertAll(
                () -> assertEquals("07:00:00 AM", updated.getSunrise()),
                () -> assertEquals("07:30:00 PM", updated.getSunset()),
                () -> assertEquals(testDate, updated.getDate()),
                () -> verify(sunriseSunsetRepository).save(existing)
        );
    }

    @Test
    void deleteSunriseSunset_ExistingId_DeletesRecord() {
        // Arrange
        when(sunriseSunsetRepository.existsById(1L)).thenReturn(true);

        // Act
        solarWatchService.deleteSunriseSunset(1L);

        // Assert
        verify(sunriseSunsetRepository).deleteById(1L);
    }

    @Test
    void getAllSunriseSunset_ReturnsAllRecords() {
        // Arrange
        SunriseSunset ss1 = createTestSunriseSunset(createTestCity());
        SunriseSunset ss2 = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.findAll()).thenReturn(List.of(ss1, ss2));

        // Act
        List<SunriseSunset> result = solarWatchService.getAllSunriseSunset();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void getSunriseAndSunset_InvalidCity_ThrowsException() {
        // Arrange
        when(cityRepository.findByName("InvalidCity")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenReturn(new GeocodingReport[0]);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("InvalidCity", testDate)
        );
    }

    @Test
    void getSunriseAndSunset_InvalidSolarData_ThrowsException() {
        // Arrange
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void deleteSunriseSunset_NonExistingId_ThrowsException() {
        // Arrange
        when(sunriseSunsetRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.deleteSunriseSunset(99L)
        );
        verify(sunriseSunsetRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateSunriseSunset_NonExistingId_ThrowsException() {
        // Arrange
        when(sunriseSunsetRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.updateSunriseSunset(99L, "07:00:00 AM", "07:30:00 PM")
        );
        verify(sunriseSunsetRepository, never()).save(any());
    }

    @Test
    void getSunriseSunsetById_NonExistingId_ReturnsEmpty() {
        // Arrange
        when(sunriseSunsetRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<SunriseSunset> result = solarWatchService.getSunriseSunsetById(99L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveSunriseSunset_ValidEntity_ReturnsSavedEntity() {
        // Arrange
        SunriseSunset newRecord = createTestSunriseSunset(createTestCity());
        when(sunriseSunsetRepository.save(newRecord)).thenReturn(newRecord);

        // Act
        SunriseSunset result = solarWatchService.saveSunriseSunset(newRecord);

        // Assert
        assertNotNull(result);
        assertEquals(newRecord.getSunrise(), result.getSunrise());
        verify(sunriseSunsetRepository).save(newRecord);
    }

    @Test
    void getSunriseAndSunset_GeocodingApiReturnsNull_ThrowsException() {
        // Arrange
        when(cityRepository.findByName("London")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(GeocodingReport[].class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void getSunriseAndSunset_SolarApiReturnsNullResults_ThrowsException() {
        // Arrange
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(new SolarWatchReport(null));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

    @Test
    void getSunriseAndSunset_SolarApiReturnsNull_ThrowsException() {
        // Arrange
        City city = createTestCity();
        when(cityRepository.findByName("London")).thenReturn(Optional.of(city));
        when(sunriseSunsetRepository.findByCityAndDate(city, testDate))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(SolarWatchReport.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                solarWatchService.getSunriseAndSunsetByGivenParameters("London", testDate)
        );
    }

}
