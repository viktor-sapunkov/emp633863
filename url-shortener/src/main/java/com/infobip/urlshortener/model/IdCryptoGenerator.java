package com.infobip.urlshortener.model;

import lombok.SneakyThrows;

import java.math.BigInteger;
import java.security.SecureRandom;

public abstract class IdCryptoGenerator<IdType extends Number> implements IdGenerator<IdType> {
    private static final int BIT_LEN = 32;

    @SneakyThrows
    protected IdCryptoGenerator() {
        this.random = SecureRandom.getInstance("SHA1PRNG");
    }

    @Override
    public IdType get() {
        return (IdType)new BigInteger(BIT_LEN, random);
    }

    private final SecureRandom random;
}