package com.example.tomatomall.exception;

import com.example.tomatomall.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(value = TomatoException.class)
    public Response<String> handleAIExternalException(TomatoException e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        return Response.buildFailure(e.getMessage(), "400");
    }
}

