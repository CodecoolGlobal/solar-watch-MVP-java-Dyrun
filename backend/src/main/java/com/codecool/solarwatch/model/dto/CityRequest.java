package com.codecool.solarwatch.model.dto;

public record CityRequest(
        String name,
        double latitude,
        double longitude,
        String state,
        String country) {
}
