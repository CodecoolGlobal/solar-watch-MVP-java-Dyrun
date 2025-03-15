package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.*;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.SunriseSunsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SolarWatchService {

    private static final String API_KEY = System.getenv("API_KEY");
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SolarWatchService.class);
    private final CityRepository cityRepository;
    private final SunriseSunsetRepository sunriseSunsetRepository;

    public SolarWatchService(RestTemplate restTemplate, CityRepository cityRepository, SunriseSunsetRepository sunriseSunsetRepository) {
        this.restTemplate = restTemplate;
        this.cityRepository = cityRepository;
        this.sunriseSunsetRepository = sunriseSunsetRepository;
    }

    public SolarWatchReportResults getSunriseAndSunsetForByGivenParameters(String cityName, LocalDate date) {
        City city = getOrCreateCity(cityName);
        SunriseSunset sunriseSunset = getOrCreateSunriseSunset(city, date);
        return new SolarWatchReportResults(sunriseSunset.getSunrise(), sunriseSunset.getSunset());
    }

    private City getOrCreateCity(String cityName) {
        Optional<City> cityOptional = cityRepository.findByName(cityName);
        if (cityOptional.isPresent()) {
            logger.info("City {} found in database", cityName);
            return cityOptional.get();
        } else {
            logger.info("City {} not found in database, calling external Geocoding API", cityName);
            String url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&appid=%s", cityName, API_KEY);
            GeocodingReport[] response = restTemplate.getForObject(url, GeocodingReport[].class);
            if (response == null || response.length == 0) {
                throw new RuntimeException("City not found by external API");
            }
            GeocodingReport report = response[0];
            City newCity = new City();
            newCity.setName(cityName);
            newCity.setLatitude(report.lat());
            newCity.setLongitude(report.lon());
            newCity.setState(report.state());
            newCity.setCountry(report.country());
            City savedCity = cityRepository.save(newCity);
            logger.info("Saved new city: {}", savedCity);
            return savedCity;
        }
    }

    private SunriseSunset getOrCreateSunriseSunset(City city, LocalDate date) {
        Optional<SunriseSunset> recordOptional = sunriseSunsetRepository.findByCityAndDate(city, date);
        if (recordOptional.isPresent()) {
            logger.info("Sunrise/Sunset for city {} on {} found in database", city.getName(), date);
            return recordOptional.get();
        } else {
            logger.info("Sunrise/Sunset for city {} on {} not found in database, calling external API", city.getName(), date);
            String url = String.format("https://api.sunrise-sunset.org/json?lat=%s&lng=%s&date=%s",
                    city.getLatitude(), city.getLongitude(), date);
            SolarWatchReport report = restTemplate.getForObject(url, SolarWatchReport.class);
            if (report == null || report.results() == null) {
                throw new RuntimeException("Unable to fetch sunrise/sunset data from external API");
            }
            SunriseSunset newRecord = new SunriseSunset();
            newRecord.setCity(city);
            newRecord.setDate(date);
            newRecord.setSunrise(report.results().sunrise());
            newRecord.setSunset(report.results().sunset());
            SunriseSunset savedRecord = sunriseSunsetRepository.save(newRecord);
            logger.info("Saved new sunrise/sunset record: {}", savedRecord);
            return savedRecord;
        }
    }
}
