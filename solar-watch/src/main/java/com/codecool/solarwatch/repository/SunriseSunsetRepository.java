package com.codecool.solarwatch.repository;

import com.codecool.solarwatch.model.entity.City;
import com.codecool.solarwatch.model.entity.SunriseSunset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SunriseSunsetRepository extends JpaRepository<SunriseSunset, Long> {
    Optional<SunriseSunset> findByCityAndDate(City city, LocalDate date);
    List<SunriseSunset> findByCity(City city);
}
