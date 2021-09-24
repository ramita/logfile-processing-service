package com.log.src.service.impl;

import com.log.src.exception.LoggerException;
import com.log.src.mapper.LoggerDataMapper;
import com.log.src.model.Event;
import com.log.src.model.LoggerData;
import com.log.src.parser.FileParser;
import com.log.src.parser.LogParser;
import com.log.src.model.ValueHolder;
import com.log.src.repository.EventRepository;
import com.log.src.service.ILoggerService;
import com.log.src.validator.PathValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class LoggerService implements ILoggerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerService.class);

    @Value("${log.config.corePoolSize:4}")
    private int threadCount;

    @Value("${log.config.queueSize:200}")
    private int queueSize;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private LoggerDataMapper loggetDataMapper;

    @Autowired
    private PathValidator pathValidator;

    private ExecutorService executorService;

    /**
     * Process and logger file information.
     */

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public List<LoggerData> getDetails(String logFilePath) {
        isFilePathValid(logFilePath);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ValueHolder valueHolder = new ValueHolder();
        BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(queueSize);


        StopWatch watch = new StopWatch();
        watch.start();

        // Start Log Parser
        for (int i = 0; i < threadCount - 1; i++) {
            executorService.submit(new LogParser(logQueue, countDownLatch, eventRepository, valueHolder));
        }

        // Start File Parser
        executorService.submit(new FileParser(logQueue, countDownLatch, valueHolder));


        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            throw new LoggerException("Error while Processing the log file.", ex);
        }
        watch.stop();
        List<Event> events = eventRepository.findAll();
        List<LoggerData> loggerDataList = events.stream().map(e -> loggetDataMapper.mapToLoggerData(e.getId(),
                e.isAlert())).collect(Collectors.toList());

        LOGGER.info("Total log records saved to db " + eventRepository.count());

        LOGGER.info("Total time taken for a file processing in ms " + watch.getTotalTimeMillis());

        return loggerDataList;
    }

    private boolean isFilePathValid(String logFilePath) {

        return pathValidator.validate(logFilePath).size() == 0;

    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }
}
