package com.creditsuisse.src.service.impl;

import com.creditsuisse.src.exception.LoggerException;
import com.creditsuisse.src.mapper.LoggetDataMapper;
import com.creditsuisse.src.model.Event;
import com.creditsuisse.src.model.LoggerData;
import com.creditsuisse.src.processor.FileProcessor;
import com.creditsuisse.src.processor.RecordProcessor;
import com.creditsuisse.src.model.ValueHolder;
import com.creditsuisse.src.repository.EventRepository;
import com.creditsuisse.src.service.ILoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class LoggerService implements ILoggerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerService.class);

    @Autowired
    private EventRepository eventRepository;

    @Value("${log.config.corePoolSize:5}")
    private int threadCount;

    @Value("${log.config.queueSize:200}")
    private int queueSize;

    @Autowired
    private LoggetDataMapper loggetDataMapper;

    /**
     * Process and logger file information.
     */
    @Override
    public List<LoggerData> getDetails() {

        ConcurrentHashMap<String, Long> hp = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ValueHolder valueHolder = new ValueHolder();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(queueSize);
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        StopWatch watch = new StopWatch();
        watch.start();
        for (int i = 0; i < threadCount - 1; i++) {
            service.submit(new RecordProcessor(queue, countDownLatch, hp, eventRepository, valueHolder));
        }
        try {
            service.submit(new FileProcessor(queue, countDownLatch, valueHolder)).get();
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error(ex.getMessage());
            throw new LoggerException(ex.getMessage(), ex);
        }
        watch.stop();
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            throw new LoggerException(ex.getMessage(), ex);
        }
        List<Event> events = eventRepository.findAll();
        List<LoggerData> loggerDataList = events.stream().map(e -> loggetDataMapper.mapToLoggerData(e.getId(),
                e.isAlert())).collect(Collectors.toList());
        LOGGER.info("Total log records saved to db " + eventRepository.count());
        LOGGER.debug("Total Elements in map" + hp.size());
        LOGGER.info("Total time taken for a file processing in ms " + watch.getTotalTimeMillis());
        service.shutdown();
        return loggerDataList;
    }
}
