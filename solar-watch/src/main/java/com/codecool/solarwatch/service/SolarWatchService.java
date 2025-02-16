package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.GeocodingReport;
import com.codecool.solarwatch.model.SolarWatchReport;
import com.codecool.solarwatch.model.SolarWatchReportResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class SolarWatchService {

    private static final String API_KEY = System.getenv("API_KEY");

    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(SolarWatchService.class);

    public SolarWatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SolarWatchReportResults getSunriseAndSunsetForByGivenParameters(String city, LocalDate date) {

        GeocodingReport geocodingReport = getGeocodingReport(city);

        String url = String.format("https://api.sunrise-sunset.org/json?lat=%s&lng=%s&date=%s", geocodingReport.lat(), geocodingReport.lon(), date);

        SolarWatchReport response = restTemplate.getForObject(url, SolarWatchReport.class);

        logger.info("Sunrise/Sunset for {} and {}", city, response);

        assert response != null;
        return new SolarWatchReportResults(
                response.results().sunrise(),
                response.results().sunset()
        );
    }

    private GeocodingReport getGeocodingReport(String city) {

        String url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&appid=%s", city, API_KEY);

        GeocodingReport[] response = restTemplate.getForObject(url, GeocodingReport[].class);

        assert response != null;
        logger.info("Geocoding report returned: {}", response[0]);

        return response[0];
    }
}
