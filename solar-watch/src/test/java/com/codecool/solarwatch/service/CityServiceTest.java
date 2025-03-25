package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    private City createTestCity(String name, double lat, double lon, String state, String country) {
        City city = new City();
        city.setName(name);
        city.setLatitude(lat);
        city.setLongitude(lon);
        city.setState(state);
        city.setCountry(country);
        return city;
    }

    @Test
    void saveCity_ValidCity_ReturnsSavedCity() {
        // Arrange
        City mockCity = createTestCity("New York", 40.7128, -74.0060, "NY", "USA");
        when(cityRepository.save(any(City.class))).thenReturn(mockCity);

        // Act
        City result = cityService.saveCity(mockCity);

        // Assert
        assertNotNull(result);
        assertEquals("New York", result.getName());
        verify(cityRepository, times(1)).save(mockCity);
    }

    @Test
    void getAllCities_ReturnsAllCities() {
        // Arrange
        List<City> mockCities = List.of(
                createTestCity("London", 51.5074, -0.1278, "England", "UK"),
                createTestCity("Paris", 48.8566, 2.3522, "Île-de-France", "France")
        );
        when(cityRepository.findAll()).thenReturn(mockCities);

        // Act
        List<City> result = cityService.getAllCities();

        // Assert
        assertEquals(2, result.size());
        verify(cityRepository, times(1)).findAll();
    }

    @Test
    void getCityById_ExistingId_ReturnsCity() {
        // Arrange
        City mockCity = createTestCity("Berlin", 52.5200, 13.4050, "Berlin", "Germany");
        when(cityRepository.findById(1L)).thenReturn(Optional.of(mockCity));

        // Act
        Optional<City> result = cityService.getCityById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Berlin", result.get().getName());
    }

    @Test
    void updateCity_ExistingId_UpdatesAndReturnsCity() {
        // Arrange
        City existingCity = createTestCity("Old City", 0.0, 0.0, "Old State", "Old Country");
        City updatedCity = createTestCity("Updated City", 1.0, 1.0, "New State", "New Country");

        when(cityRepository.findById(1L)).thenReturn(Optional.of(existingCity));
        when(cityRepository.save(any(City.class))).thenReturn(existingCity);

        // Act
        City result = cityService.updateCity(1L, updatedCity);

        // Assert
        ArgumentCaptor<City> captor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(captor.capture());

        City savedCity = captor.getValue();
        assertEquals("Updated City", savedCity.getName());
        assertEquals(1.0, savedCity.getLatitude());
        assertEquals("New Country", savedCity.getCountry());
        assertSame(existingCity, result);
    }

    @Test
    void getCityByName_ExistingName_ReturnsCity() {
        // Arrange
        City mockCity = createTestCity("Tokyo", 35.6762, 139.6503, "Kanto", "Japan");
        when(cityRepository.findByName("Tokyo")).thenReturn(Optional.of(mockCity));

        // Act
        Optional<City> result = cityService.getCityByName("Tokyo");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Japan", result.get().getCountry());
    }

    @Test
    void getCityById_NonExistingId_ReturnsEmpty() {
        // Arrange
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<City> result = cityService.getCityById(99L);

        // Assert
        assertTrue(result.isEmpty());
        verify(cityRepository, times(1)).findById(99L);
    }

    @Test
    void updateCity_NonExistingId_ThrowsException() {
        // Arrange
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());
        City updatedCity = createTestCity("Updated City", 1.0, 1.0, "New State", "New Country");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cityService.updateCity(99L, updatedCity);
        });
        verify(cityRepository, never()).save(any());
    }

    @Test
    void deleteCity_ExistingId_DeletesCity() {
        // Arrange
        when(cityRepository.existsById(1L)).thenReturn(true);

        // Act
        cityService.deleteCity(1L);

        // Assert
        verify(cityRepository, times(1)).deleteById(1L);
        verify(cityRepository, times(1)).existsById(1L);
    }

    @Test
    void deleteCity_NonExistingId_ThrowsException() {
        // Arrange
        when(cityRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cityService.deleteCity(99L);
        });
        verify(cityRepository, never()).deleteById(anyLong());
        verify(cityRepository, times(1)).existsById(99L);
    }

}