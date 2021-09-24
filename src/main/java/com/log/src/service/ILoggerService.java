package com.log.src.service;

import com.log.src.model.LoggerData;

import java.util.List;

public interface ILoggerService {
    List<LoggerData> getDetails(String logFilePath);
}
