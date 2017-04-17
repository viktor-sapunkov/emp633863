package com.infobip.urlshortener.model;

import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class StatUnitIdGenerator extends IdCryptoGenerator<BigInteger> {
}
