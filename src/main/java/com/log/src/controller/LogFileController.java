package com.log.src.controller;

import com.log.src.model.LoggerData;
import com.log.src.service.ILoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class LogFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileController.class);

    @Autowired
    ILoggerService loggerService;

    @GetMapping("/log/process")
    public ResponseEntity<List<LoggerData>> getDetails(@RequestParam("file") MultipartFile multipartFile) {
        LOGGER.info("Logger Processing request received.");
        List<LoggerData> loggerData = loggerService.getDetails(multipartFile);
        LOGGER.info("Logger Processing completed.");
        return new ResponseEntity<>(loggerData, HttpStatus.OK);
    }
}
