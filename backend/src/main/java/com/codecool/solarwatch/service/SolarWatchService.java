package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.dto.GeocodingReport;
import com.codecool.solarwatch.model.dto.SolarWatchReport;
import com.codecool.solarwatch.model.dto.SolarWatchReportResults;
import com.codecool.solarwatch.model.dto.SunriseSunsetResponse;
import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import com.codecool.solarwatch.repository.CityRepository;
import com.codecool.solarwatch.repository.SunriseSunsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SolarWatchService {

    private static final String API_KEY = System.getenv("API_KEY");
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SolarWatchService.class);
    private final CityRepository cityRepository;
    private final SunriseSunsetRepository sunriseSunsetRepository;

    @Value("${api.openweathermap.url}")
    private String openWeatherMapUrl;

    @Value("${api.sunrise-sunset.url}")
    private String sunriseSunsetUrl;

    public SolarWatchService(RestTemplate restTemplate, CityRepository cityRepository, SunriseSunsetRepository sunriseSunsetRepository) {
        this.restTemplate = restTemplate;
        this.cityRepository = cityRepository;
        this.sunriseSunsetRepository = sunriseSunsetRepository;
    }

    public SolarWatchReportResults getSunriseAndSunsetByGivenParameters(String cityName, LocalDate date) {
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
            String url = String.format("%s/geo/1.0/direct?q=%s&appid=%s", openWeatherMapUrl, cityName, API_KEY);
            GeocodingReport[] response = restTemplate.getForObject(url, GeocodingReport[].class);
            logger.info("response = {}", response[0].toString());
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
            String url = String.format("%s/json?lat=%s&lng=%s&date=%s",
                    sunriseSunsetUrl ,city.getLatitude(), city.getLongitude(), date);
            SolarWatchReport report = restTemplate.getForObject(url, SolarWatchReport.class);
            logger.info("response = {}", report.toString());
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

    public SunriseSunset saveSunriseSunset(SunriseSunset sunriseSunset) {
        return sunriseSunsetRepository.save(sunriseSunset);
    }

    public List<SunriseSunset> getAllSunriseSunset() {
        return sunriseSunsetRepository.findAll();
    }

    public Optional<SunriseSunset> getSunriseSunsetById(Long id) {
        return sunriseSunsetRepository.findById(id);
    }

    public SunriseSunset updateSunriseSunset(Long id, String newSunrise, String newSunset) {
        Optional<SunriseSunset> SunriseSunset = sunriseSunsetRepository.findById(id);
        if (SunriseSunset.isPresent()) {
            SunriseSunset sunriseSunset = SunriseSunset.get();
            sunriseSunset.setSunrise(newSunrise);
            sunriseSunset.setSunset(newSunset);
            return sunriseSunsetRepository.save(sunriseSunset);
        } else {
            throw new RuntimeException("Sunrise/Sunset record not found.");
        }
    }

    public void deleteSunriseSunset(Long id) {
        if (sunriseSunsetRepository.existsById(id)) {
            sunriseSunsetRepository.deleteById(id);
        } else {
            throw new RuntimeException("Record not found.");
        }
    }

    public List<SunriseSunsetResponse> getSunriseSunsetByCity(String cityName) {
        City city = cityRepository.findByName(cityName).orElseThrow();
        List<SunriseSunset> sunriseSunsets = sunriseSunsetRepository.findByCity(city);

        return sunriseSunsets.stream().map(sunriseSunset -> new SunriseSunsetResponse(
                sunriseSunset.getSunrise(),
                sunriseSunset.getSunset(),
                sunriseSunset.getDate()
        )).toList();
    }
}
