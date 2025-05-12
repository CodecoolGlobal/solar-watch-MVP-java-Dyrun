package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.dto.*;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.SunriseSunsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class SolarWatchService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SolarWatchService.class);
    private final CityRepository cityRepository;
    private final SunriseSunsetRepository sunriseSunsetRepository;

    @Value("${api.key}")
    private String API_KEY;

    @Value("${api.openweathermap.url}")
    private String openWeatherMapUrl;

    @Value("${api.sunrise-sunset.url}")
    private String sunriseSunsetUrl;

    public SolarWatchService(RestTemplate restTemplate, CityRepository cityRepository, SunriseSunsetRepository sunriseSunsetRepository) {
        this.restTemplate = restTemplate;
        this.cityRepository = cityRepository;
        this.sunriseSunsetRepository = sunriseSunsetRepository;
    }

    public SolarWatchResponse getSunriseAndSunsetByGivenParameters(String cityName, LocalDate date) {
        City city = getOrCreateCity(cityName);
        SunriseSunset sunriseSunset = getOrCreateSunriseSunset(city, date);
        return new SolarWatchResponse(sunriseSunset.getSunrise(), sunriseSunset.getSunset(), date, city.getName(), city.getCountry());
    }

    private City getOrCreateCity(String cityName) {
        Optional<City> cityOptional = cityRepository.findByName(cityName);
        if (cityOptional.isPresent()) {
            logger.info("City {} found in database", cityName);
            return cityOptional.get();
        } else {
            GeocodingReport report = fetchGeocodingData(cityName);
            return saveCity(report);
        }
    }

    private GeocodingReport fetchGeocodingData(String cityName) {
        logger.info("City {} not found in database, calling external Geocoding API", cityName);
        String url = String.format("%s/geo/1.0/direct?q=%s&appid=%s", openWeatherMapUrl, cityName, API_KEY);
        GeocodingReport[] response = restTemplate.getForObject(url, GeocodingReport[].class);
        if (response == null || response.length == 0) {
            throw new NoSuchElementException("City not found by external API");
        }
        logger.info("response = {}\nurl: {}", response[0].toString(), url);
        return response[0];
    }

    private City saveCity(GeocodingReport report) {
        Optional<City> city = cityRepository.findByName(report.name());
        if (city.isPresent()) return city.orElseThrow(() -> new NoSuchElementException("City " + report.name() + " not found in database"));
        City newCity = new City();
        newCity.setName(report.name());
        newCity.setLatitude(report.lat());
        newCity.setLongitude(report.lon());
        newCity.setState(report.state());
        newCity.setCountry(report.country());
        City savedCity = cityRepository.save(newCity);
        logger.info("Saved new city: {}", savedCity);
        return savedCity;
    }

    private SunriseSunset getOrCreateSunriseSunset(City city, LocalDate date) {
        Optional<SunriseSunset> recordOptional = sunriseSunsetRepository.findByCityAndDate(city, date);
        if (recordOptional.isPresent()) {
            logger.info("Sunrise/Sunset for city {} on {} found in database", city.getName(), date);
            return recordOptional.get();
        } else {
            SunriseSunsetReport report = fetchSunriseSunset(city, date);
            return saveSunriseSunset(city, date, report);
        }
    }

    private SunriseSunsetReport fetchSunriseSunset(City city, LocalDate date) {
        logger.info("Sunrise/Sunset for city {} on {} not found in database, calling external API", city.getName(), date);
        String url = String.format("%s/json?lat=%s&lng=%s&date=%s&formatted=0",
                sunriseSunsetUrl, city.getLatitude(), city.getLongitude(), date);
        SunriseSunsetReport report = restTemplate.getForObject(url, SunriseSunsetReport.class);
        if (report == null || report.results() == null) {
            throw new NoSuchElementException("Unable to fetch sunrise/sunset data from external API");
        }
        logger.info("response = {}\nurl:{}", report.toString(), url);
        return report;
    }

    private SunriseSunset saveSunriseSunset(City city, LocalDate date, SunriseSunsetReport report) {
        SunriseSunset newRecord = new SunriseSunset();
        newRecord.setCity(city);
        newRecord.setDate(date);
        newRecord.setSunrise(report.results().sunrise());
        newRecord.setSunset(report.results().sunset());
        SunriseSunset savedRecord = sunriseSunsetRepository.save(newRecord);
        logger.info("Saved new sunrise/sunset record: {}", savedRecord);
        return savedRecord;
    }

    public SunriseSunset saveSunriseSunset(SunriseSunsetRequest sunriseSunset) {
        City city = cityRepository.findByName(sunriseSunset.cityName()).orElseThrow(() -> new NoSuchElementException("City " + sunriseSunset.cityName() + " not found"));
        SunriseSunset newRecord = new SunriseSunset();
        newRecord.setCity(city);
        newRecord.setDate(sunriseSunset.date());
        newRecord.setSunrise(sunriseSunset.sunrise());
        newRecord.setSunset(sunriseSunset.sunset());
        return sunriseSunsetRepository.save(newRecord);
    }

    public List<SunriseSunset> getAllSunriseSunset() {
        return sunriseSunsetRepository.findAll();
    }

    public SunriseSunset getSunriseSunsetById(Long id) {
        return sunriseSunsetRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No data found with id: " + id.toString()));
    }

    @Transactional
    public SunriseSunset updateSunriseSunset(Long id, String newSunrise, String newSunset) {
        Optional<SunriseSunset> SunriseSunset = sunriseSunsetRepository.findById(id);
        if (SunriseSunset.isPresent()) {
            SunriseSunset sunriseSunset = SunriseSunset.get();
            sunriseSunset.setSunrise(newSunrise);
            sunriseSunset.setSunset(newSunset);
            return sunriseSunsetRepository.save(sunriseSunset);
        } else {
            throw new NoSuchElementException("Sunrise/Sunset record not found with id: " + id);
        }
    }

    public void deleteSunriseSunset(Long id) {
        if (sunriseSunsetRepository.existsById(id)) {
            sunriseSunsetRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Record not found with id: " + id);
        }
    }

    public List<SunriseSunsetResponse> getSunriseSunsetByCity(String cityName) {
        City city = cityRepository.findByName(cityName).orElseThrow(() -> new NoSuchElementException("City not found: " + cityName));
        List<SunriseSunset> sunriseSunsets = sunriseSunsetRepository.findByCity(city);

        return sunriseSunsets.stream().map(sunriseSunset -> new SunriseSunsetResponse(
                sunriseSunset.getSunrise(),
                sunriseSunset.getSunset(),
                sunriseSunset.getDate()
        )).toList();
    }
}
