package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.service.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/city")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public City createCity(@RequestBody City city) {
        return cityService.saveCity(city);
    }

    @GetMapping("/all")
    public List<City> getAllCities() {
        return cityService.getAllCities();
    }

    @GetMapping("/{id}")
    public City getCityById(@PathVariable Long id) {
        return cityService.getCityById(id);
    }

    @GetMapping
    public City getCityByName(@RequestParam String name) {
        return cityService.getCityByName(name);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public City updateCity(@PathVariable Long id, @RequestBody City updatedCity) {
        return cityService.updateCity(id, updatedCity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
    }
}
