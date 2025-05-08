package com.codecool.solarwatch.model.dto;

import java.time.LocalDate;

public record SunriseSunsetResponse(String sunrise, String sunset, LocalDate date) {
}
