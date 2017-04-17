package com.infobip.urlshortener.model;

public enum RedirectType {
    MOVED_PERMANENTLY(301), FOUND(302);

    RedirectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private final int value;
}
