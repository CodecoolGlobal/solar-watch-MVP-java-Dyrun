package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public City getCityById(Long id) {
        return cityRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No city found with id: " + id));
    }

    public City getCityByName(String name) {
        return cityRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("No city found with id: " + name));
    }

    public City updateCity(Long id, City updatedCity) {
        Optional<City> optionalCity = cityRepository.findById(id);
        if (optionalCity.isPresent()) {
            City city = optionalCity.get();
            city.setName(updatedCity.getName());
            city.setLatitude(updatedCity.getLatitude());
            city.setLongitude(updatedCity.getLongitude());
            city.setState(updatedCity.getState());
            city.setCountry(updatedCity.getCountry());
            return cityRepository.save(city);
        } else {
            throw new NoSuchElementException("No city found with id: " + id);
        }
    }

    public void deleteCity(Long id) {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("No city found with id: " + id);
        }
    }
}
