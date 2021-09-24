package com.log.src.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.src.constant.Error;
import com.log.src.exception.LoggerException;
import com.log.src.model.Event;
import com.log.src.model.ValueHolder;
import com.log.src.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class LogParser implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);

    private final BlockingQueue<String> blockingQueue;
    private CountDownLatch countDownLatch;

    private ValueHolder valueHolder;
    private ConcurrentHashMap<String, Long> eventData ;
    private EventRepository eventRepository;

    public LogParser(BlockingQueue<String> blockingQueue,
                     CountDownLatch countDownLatch,
                     ConcurrentHashMap<String, Long> eventData,
                     EventRepository eventRepository,
                     ValueHolder valueHolder) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;
        this.eventData = eventData;
        this.eventRepository = eventRepository;
        this.valueHolder = valueHolder;
    }

    /**
     * Process records for log file.
     */
    @Override
    public Integer call() throws Exception  {
        ObjectMapper objectMapper = new ObjectMapper();
        Event event ;
        while (true) {
            if (valueHolder.isFlag() && blockingQueue.isEmpty()) {
                break;
            }
            try {
                if (!blockingQueue.isEmpty()) {
                    event = objectMapper.readValue(blockingQueue.take(), Event.class);
                    process(event);
                }
            } catch (IOException | InterruptedException ex) {
                LOGGER.error(ex.getMessage());
                countDownLatch.countDown();
                throw new LoggerException(Error.LOG_FILE_INFORMAT.msg);
            }
        }
        countDownLatch.countDown();
        LOGGER.info("Log File Reading finished");

        return 0;
    }

    private void process(Event event) {
        synchronized (LogParser.class) {
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
