package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.dto.CityRequest;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City saveCity(CityRequest city) {
        City newCity = new City();
        newCity.setName(city.name());
        newCity.setCountry(city.country());
        newCity.setState(city.state());
        newCity.setLatitude(city.latitude());
        newCity.setLongitude(city.longitude());
        return cityRepository.save(newCity);
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

    @Transactional
    public City updateCity(Long id, CityRequest updatedCity) {
        Optional<City> optionalCity = cityRepository.findById(id);
        if (optionalCity.isPresent()) {
            City city = optionalCity.get();
            city.setName(updatedCity.name());
            city.setLatitude(updatedCity.latitude());
            city.setLongitude(updatedCity.longitude());
            city.setState(updatedCity.state());
            city.setCountry(updatedCity.country());
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
