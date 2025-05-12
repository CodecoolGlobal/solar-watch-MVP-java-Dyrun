package com.codecool.solarwatch.model.dto;

import java.time.LocalDate;

public record SunriseSunsetRequest(
        String cityName,
        String sunrise,
        String sunset,
        LocalDate date) {
}
