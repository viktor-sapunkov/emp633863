package com.infobip.urlshortener.model;

import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public final class UserIdGenerator extends IdCryptoGenerator<BigInteger> {
}
