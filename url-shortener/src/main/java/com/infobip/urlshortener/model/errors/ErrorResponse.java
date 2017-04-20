package com.infobip.urlshortener.model.errors;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String code, message;
}
