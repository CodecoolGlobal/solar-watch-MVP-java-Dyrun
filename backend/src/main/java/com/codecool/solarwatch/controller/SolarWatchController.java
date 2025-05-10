package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.dto.SolarWatchResponse;
import com.codecool.solarwatch.model.dto.SunriseSunsetResponse;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.service.SolarWatchService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sunrise-sunset")
public class SolarWatchController {

    private final SolarWatchService solarWatchService;

    public SolarWatchController(SolarWatchService solarWatchService) {
        this.solarWatchService = solarWatchService;
    }

    @GetMapping
    public SolarWatchResponse getSunriseSunset(@RequestParam(defaultValue = "Budapest") String city,
                                               @RequestParam String date) {
        return solarWatchService.getSunriseAndSunsetByGivenParameters(city, LocalDate.parse(date));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SunriseSunset createSunriseSunset(@RequestBody SunriseSunset sunriseSunset) {
        return solarWatchService.saveSunriseSunset(sunriseSunset);
    }

    @GetMapping("/all")
    public List<SunriseSunset> getAllSunriseSunsets() {
        return solarWatchService.getAllSunriseSunset();
    }

    @GetMapping("/{id}")
    public SunriseSunset getSunriseSunsetById(@PathVariable Long id) {
        return solarWatchService.getSunriseSunsetById(id);
    }

    @GetMapping("/city")
    public List<SunriseSunsetResponse> getSunriseSunsetByCity(@RequestParam String cityName) {
        return solarWatchService.getSunriseSunsetByCity(cityName);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SunriseSunset updateSunriseSunset(@PathVariable Long id,
                                             @RequestParam String sunrise,
                                             @RequestParam String sunset) {
        return solarWatchService.updateSunriseSunset(id, sunrise, sunset);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSunriseSunset(@PathVariable Long id) {
        solarWatchService.deleteSunriseSunset(id);
    }
}
