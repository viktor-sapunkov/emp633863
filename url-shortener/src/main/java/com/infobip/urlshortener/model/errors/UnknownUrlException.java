package com.infobip.urlshortener.model.errors;

import com.infobip.urlshortener.model.UrlShortener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Short URL is unknown")
public class UnknownUrlException extends RuntimeException {
    public UnknownUrlException(String message) {
        super(message);
    }

    public static <StatUnitId> Supplier<UnknownUrlException> of(StatUnitId urlId) {
        return () -> new UnknownUrlException(String.format(
            "%s is an unknown short URL", UrlShortener.getShortenedUrl(urlId)
        ));
    }
}
