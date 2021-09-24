package com.log.src.exception;

public class LoggerException extends RuntimeException {
    public LoggerException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public LoggerException(String errorMessage) {
        super(errorMessage);
    }
}
