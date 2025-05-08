package com.codecool.solarwatch.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingReport(double lat, double lon, String name, String country, String state) {
}
