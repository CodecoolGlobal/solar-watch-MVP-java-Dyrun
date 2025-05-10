package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.dto.ErrorMsg;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class SolarWatchControllerAdvice {

    @ResponseBody
    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMsg dateTimeParseExceptionHandler(DateTimeParseException ex) {
        return new ErrorMsg(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMsg nullPointerExceptionHandler(NullPointerException ex) {
        return new ErrorMsg(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMsg noSuchElementException(NoSuchElementException ex) {
        return new ErrorMsg(ex.getMessage());
    }

}
