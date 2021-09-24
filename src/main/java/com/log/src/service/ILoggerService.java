package com.log.src.service;

import com.log.src.model.LoggerData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ILoggerService {
    List<LoggerData> getDetails(MultipartFile multipartFile );
}
