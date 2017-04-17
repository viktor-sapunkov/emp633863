package com.infobip.urlshortener.model;

import java.util.function.Supplier;

public interface IdGenerator<IdType> extends Supplier<IdType> {
}
