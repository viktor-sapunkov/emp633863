package com.infobip.urlshortener.model.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unknown redirect type requested")
public class AmbiguousRedirectTypeException extends RuntimeException {
    public AmbiguousRedirectTypeException(String message) {
        super(message);
    }
}
