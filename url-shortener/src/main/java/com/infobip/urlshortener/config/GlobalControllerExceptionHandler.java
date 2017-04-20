package com.infobip.urlshortener.config;

import com.infobip.urlshortener.model.errors.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    @RequestMapping(produces = "application/json")
    public ErrorResponse catchAll(Exception e) {
        LOGGER.error("Service unavailable", e);
        return new ErrorResponse(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE), String.valueOf(e));
    }
}
