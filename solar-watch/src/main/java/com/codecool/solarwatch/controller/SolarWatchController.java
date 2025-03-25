package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.dto.SolarWatchReportResults;
import com.codecool.solarwatch.model.dto.SunriseSunsetResponse;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.service.SolarWatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sunrise-sunset")
public class SolarWatchController {

    private final SolarWatchService solarWatchService;

    public SolarWatchController(SolarWatchService solarWatchService) {
        this.solarWatchService = solarWatchService;
    }

    @GetMapping
    public ResponseEntity<?> getSunriseSunset(@RequestParam(defaultValue = "Budapest") String city,
                                              @RequestParam String date) {
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetByGivenParameters(city, LocalDate.parse(date));
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SunriseSunset> createSunriseSunset(@RequestBody SunriseSunset sunriseSunset) {
        return ResponseEntity.ok(solarWatchService.saveSunriseSunset(sunriseSunset));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SunriseSunset>> getAllSunriseSunsets() {
        return ResponseEntity.ok(solarWatchService.getAllSunriseSunset());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SunriseSunset> getSunriseSunsetById(@PathVariable Long id) {
        Optional<SunriseSunset> sunriseSunset = solarWatchService.getSunriseSunsetById(id);
        return sunriseSunset.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    public ResponseEntity<List<SunriseSunsetResponse>> getSunriseSunsetByCity(@RequestParam String city) {
        return ResponseEntity.ok(solarWatchService.getSunriseSunsetByCity(city));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SunriseSunset> updateSunriseSunset(@PathVariable Long id,
                                                             @RequestParam String sunrise,
                                                             @RequestParam String sunset) {
        return ResponseEntity.ok(solarWatchService.updateSunriseSunset(id, sunrise, sunset));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSunriseSunset(@PathVariable Long id) {
        solarWatchService.deleteSunriseSunset(id);
        return ResponseEntity.noContent().build();
    }
}
