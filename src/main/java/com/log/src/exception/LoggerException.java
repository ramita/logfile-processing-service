package com.creditsuisse.src.exception;

public class LoggerException extends RuntimeException {
    public LoggerException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
