package com.log.src.processor;

import com.log.src.exception.LoggerException;
import com.log.src.model.Event;
import com.log.src.model.ValueHolder;
import com.log.src.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class RecordProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordProcessor.class);

    private final BlockingQueue<String> blockingQueue;
    private CountDownLatch countDownLatch;
    private Map<String, Long> hp;
    private ValueHolder valueHolder;
    private EventRepository eventRepository;

    public RecordProcessor(BlockingQueue<String> blockingQueue,
                           CountDownLatch countDownLatch,
                           Map<String, Long> hp,
                           EventRepository eventRepository,
                           ValueHolder valueHolder) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;
        this.hp = hp;
        this.eventRepository = eventRepository;
        this.valueHolder = valueHolder;
    }

    /**
     * Process records for log file.
     */
    @Override
    public void run() {
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
                throw new LoggerException(ex.getMessage(), ex);
            }
        }
        countDownLatch.countDown();
        LOGGER.info("CPU finished");
    }

    private void process(Event event) {
        synchronized (RecordProcessor.class) {
            String newEventId = event.getId();
            if (hp.containsKey(newEventId)) {
                Instant startInstant = Instant.ofEpochMilli(event.getTimestamp());
                Instant endInstance = Instant.ofEpochMilli(hp.get(newEventId));
                long delta = Duration.between(startInstant, endInstance).toMillis();
                if (delta > 4) {
                    event.setAlert(true);
                } else {
                    event.setAlert(false);
                }
                eventRepository.save(event);
                hp.remove(newEventId);
            } else {
                hp.put(event.getId(), event.getTimestamp());
            }
        }
    }
}
