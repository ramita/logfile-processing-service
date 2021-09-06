package com.creditsuisse.src.controller;

import com.creditsuisse.src.model.LoggerData;
import com.creditsuisse.src.service.ILoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class LogEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEvent.class);

    @Autowired
    ILoggerService loggerService;

    @GetMapping("/log/process")
    public ResponseEntity<List<LoggerData>> getDetails() {
        LOGGER.info("Logger Processing request received");
        List<LoggerData> loggerData = loggerService.getDetails();
        return new ResponseEntity<>(loggerData, HttpStatus.OK);
    }
}
