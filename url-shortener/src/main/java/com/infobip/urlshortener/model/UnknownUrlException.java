package com.infobip.urlshortener.model;

public class UnknownUrlException extends RuntimeException {
    public UnknownUrlException() {
    }

    public UnknownUrlException(String message) {
        super(message);
    }

    public UnknownUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownUrlException(Throwable cause) {
        super(cause);
    }

    public UnknownUrlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
