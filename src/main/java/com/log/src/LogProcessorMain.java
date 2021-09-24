package com.log.src;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LogProcessorMain {
    public static void main(String[] args) {
        SpringApplication.run(LogProcessorMain.class, args);
    }
}
