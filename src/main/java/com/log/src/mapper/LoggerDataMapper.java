package com.log.src.mapper;

import com.log.src.model.LoggerData;
import org.springframework.stereotype.Component;

@Component
public class LoggetDataMapper {

    /**
     * Map and return loggerData.
     */
    public LoggerData mapToLoggerData(String id, boolean alert) {
        LoggerData loggerData = new LoggerData();
        loggerData.setAlert(alert);
        loggerData.setId(id);
        return loggerData;

    }
}
