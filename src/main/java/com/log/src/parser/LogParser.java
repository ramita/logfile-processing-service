package com.log.src.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.src.model.Event;
import com.log.src.model.ValueHolder;
import com.log.src.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;


public class LogParser implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);

    private final BlockingQueue<String> blockingQueue;
    private CountDownLatch countDownLatch;

    private ValueHolder valueHolder;
    private ConcurrentMap<String, Long> eventData;
    private EventRepository eventRepository;
    private ObjectMapper objectMapper;

    public LogParser(BlockingQueue<String> blockingQueue, CountDownLatch countDownLatch,
                     ConcurrentMap<String, Long> eventData, EventRepository eventRepository, ValueHolder valueHolder) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;
        this.eventData = eventData;
        this.eventRepository = eventRepository;
        this.valueHolder = valueHolder;
        objectMapper = new ObjectMapper();
    }

    /**
     * Process records for log file.
     */
    @Override
    public Boolean call() {
        LOGGER.info(" Log Parser Thread Started " + Thread.currentThread().getName());
        Event event = null;
        while (true) {
            if (valueHolder.isFileReadingCompleted() && blockingQueue.isEmpty() || valueHolder.isErrorInRecord()) {
                countDownLatch.countDown();
                break;
            } else {
                try {
                    event = objectMapper.readValue(blockingQueue.poll(60, TimeUnit.SECONDS), Event.class);
                } catch (IOException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage());
                    valueHolder.setErrorInRecord(true);
                }
            }
            process(event);
        }

        LOGGER.info("Log File Reading finished. " + Thread.currentThread().getName());

        return valueHolder.isErrorInRecord();
    }

    private void process(Event event) {
        synchronized (LogParser.class) {
            if (Objects.nonNull(event)) {
                String newEventId = event.getId();
                if (eventData.containsKey(newEventId)) {
                    Instant startInstant = Instant.ofEpochMilli(event.getTimestamp());
                    Instant endInstance = Instant.ofEpochMilli(eventData.get(newEventId));
                    long delta = Duration.between(startInstant, endInstance).toMillis();
                    if (delta > 4)
                        event.setAlert(true);
                    else {
                        event.setAlert(false);
                    }
                    eventRepository.save(event);
                    eventData.remove(newEventId);
                } else {
                    eventData.put(event.getId(), event.getTimestamp());
                }
            }

        }
    }

}
