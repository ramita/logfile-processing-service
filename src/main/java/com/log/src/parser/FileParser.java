package com.log.src.processor;

import com.log.src.exception.LoggerException;
import com.log.src.model.ValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class FileProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);

    private final BlockingQueue<String> blockingQueue;
    private CountDownLatch countDownLatch;
    private ValueHolder valueHolder;

    public FileProcessor(BlockingQueue<String> blockingQueue, CountDownLatch countDownLatch, ValueHolder valueHolder) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;

        this.valueHolder = valueHolder;

    }

    /**
     * Read log file.
     */
    @Override
    public void run() {

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            File file = ResourceUtils.getFile("classpath:logfile.txt");
            inputStream = new FileInputStream(file);
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
        countDownLatch.countDown();
        valueHolder.setFlag(true);
        LOGGER.info("Log File Processing completed.");
    }
}
