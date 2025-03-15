package com.codecool.solarwatch.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sunrise_sunset")
public class SunriseSunset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;
    private String sunrise;
    private String sunset;
    private LocalDate date;

    public City getCity() {
        return city;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }
}
