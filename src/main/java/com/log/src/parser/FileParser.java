package com.log.src.parser;

import com.log.src.exception.LoggerException;
import com.log.src.model.ValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class FileParser implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileParser.class);

    private final BlockingQueue<String> blockingQueue;
    private CountDownLatch countDownLatch;
    private ValueHolder valueHolder;
    private MultipartFile file;

    public FileParser(BlockingQueue<String> blockingQueue, CountDownLatch countDownLatch, ValueHolder valueHolder,
                      MultipartFile file) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;
        this.valueHolder = valueHolder;
        this.file = file;
    }

    /**
     * Read log file.
     */
    @Override
    public void run() {
        LOGGER.info(" File Parser Thread started "+Thread.currentThread().getName());
        InputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = file.getInputStream();
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                LOGGER.info(line);
                blockingQueue.put(line);
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex.getMessage());
            throw new LoggerException(ex.getMessage(), ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                    throw new LoggerException(ex.getMessage(), ex);
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        valueHolder.setFileReadingCompleted(true);
        countDownLatch.countDown();
        LOGGER.info("Log File Processing completed.");
    }
}
