package com.infobip.urlshortener.model;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String code, message;
}
