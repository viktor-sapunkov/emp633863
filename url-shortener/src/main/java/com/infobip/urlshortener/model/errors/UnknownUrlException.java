package com.infobip.urlshortener.model.errors;

import com.infobip.urlshortener.model.UrlShortener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Short URL is unknown")
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

    public static <StatUnitId> Supplier<UnknownUrlException> of(StatUnitId urlId) {
        return () -> new UnknownUrlException(String.format(
            "%s is an unknown short URL", UrlShortener.getShortenedUrl(urlId)
        ));
    }
}
