package com.log.src.exception.handler;

import com.log.src.exception.LoggerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LogExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Method to handle exception.
     */
    @ExceptionHandler(LoggerException.class)
    public ResponseEntity<Object> handleExceptions(LoggerException ex) {

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);

    }
}
