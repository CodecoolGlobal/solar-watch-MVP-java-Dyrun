package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.SolarWatchReportResults;
import com.codecool.solarwatch.service.SolarWatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class SolarWatchController {

    private final SolarWatchService solarWatchService;

    public SolarWatchController(SolarWatchService solarWatchService) {
        this.solarWatchService = solarWatchService;
    }

    @GetMapping("/sunrise-sunset")
    public ResponseEntity<?> getSunriseSunset(@RequestParam(defaultValue = "Budapest") String city,
                                              @RequestParam String date) {
        SolarWatchReportResults result = solarWatchService.getSunriseAndSunsetForByGivenParameters(city, LocalDate.parse(date));
        return ResponseEntity.ok(result);
    }
}
