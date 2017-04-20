package com.infobip.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.infobip.urlshortener.model.errors.AmbiguousRedirectTypeException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum RedirectType {
    MOVED_PERMANENTLY(301), FOUND(302);

    RedirectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private final int value;

    @JsonCreator
    public static RedirectType forValue(int value) {
        List<RedirectType> matches = Stream.of(RedirectType.values())
                .filter(redirectType -> redirectType.getValue() == value)
                .collect(Collectors.toList());

        if (matches.size() != 1) {
            throw new AmbiguousRedirectTypeException(String.valueOf(value));
        }
        return matches.get(0);
    }
}
