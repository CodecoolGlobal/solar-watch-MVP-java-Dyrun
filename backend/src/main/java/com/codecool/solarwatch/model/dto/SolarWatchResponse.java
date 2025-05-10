package com.codecool.solarwatch.model.dto;

import java.time.LocalDate;

public record SolarWatchResponse(String sunrise, String sunset, LocalDate date, String cityName, String country) {
}
