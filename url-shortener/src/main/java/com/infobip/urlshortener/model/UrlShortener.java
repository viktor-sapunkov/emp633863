package com.infobip.urlshortener.model;

import com.infobip.urlshortener.URLShortenerApplication;
import com.infobip.urlshortener.model.errors.UnknownUrlException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

@Component
public final class UrlShortener<UserId extends Number, StatUnitId extends BigInteger> {
    public static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            new String[]{"register", "account", "statistic", "favicon", "help", "css", "img", "js"}));

    public static final char[] DIGITS = URLShortenerApplication.ALPHANUMERIC;
    public static final BigInteger NUM_DIGITS = new BigInteger(String.valueOf(DIGITS.length));
    public static final Map<Character, BigInteger> VALUES;

    @Inject
    public UrlShortener(final StatUnitIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public StatUnitId shorten(final UserId subject, final String url, final RedirectType redirectType,
                              final Runnable collisionsHandler) {

        Optional<StatUnitId> shortened;
        StatUnitId key;

        for (;;) {
            shortened = ofNullable(lengthyUrlToShortenedId.get(url));
            if (shortened.isPresent()) {
                collisionsHandler.run();
                return shortened.get();
            }

            shortened = Optional.of((StatUnitId) idGenerator.get());
            key = shortened.get();

            if (stopWordsMatch(key)) {
                continue;
            }

            ShortenedEntry newEntry = new ShortenedEntry<>(subject, url, redirectType);
            if (urls.putIfAbsent(key, newEntry) == null) {
                lengthyUrlToShortenedId.put(url, key);
                break;
            }
        }

        Queue<StatUnitId> allKeysOfThisSubject =
                owned.putIfAbsent(subject, new ConcurrentLinkedQueue<>(Collections.singletonList(key)));

        if (allKeysOfThisSubject != null) {
            allKeysOfThisSubject.add(key);
        }
        return shortened.get();
    }

    public boolean stopWordsMatch(StatUnitId key) {
        String converted = getShortenedUrl(key);
        return STOP_WORDS.contains(converted);
    }

    public void urlVisited(final StatUnitId urlId) {
        ofNullable(urls.get(urlId))
                .map(ShortenedEntry::getVisits)
                .map(AtomicLong::incrementAndGet)
                .orElseThrow(UnknownUrlException.of(urlId));
    }

    public long getHits(final StatUnitId urlId) {
        return ofNullable(urls.get(urlId))
                .map(ShortenedEntry::getVisits)
                .map(AtomicLong::longValue)
                .orElse(0L);
    }

    public Queue<StatUnitId> getAllShortKeysByUserId(final UserId userId) {
        return ofNullable(owned.get(userId)).orElseGet(ArrayDeque::new);
    }

    public String getOriginalUrl(final StatUnitId urlId) {
        return ofNullable(urls.get(urlId))
                .map(ShortenedEntry::getOriginalUrl)
                .orElseThrow(UnknownUrlException.of(urlId));
    }

    public RedirectType getRedirectType(final StatUnitId urlId) {
        return ofNullable(urls.get(urlId))
                .map(ShortenedEntry::getRedirectType)
                .orElseThrow(UnknownUrlException.of(urlId));
    }

    public static <StatUnitId> String getShortenedUrl(final StatUnitId key) {
        final StringBuilder result = new StringBuilder();
        BigInteger value = new BigInteger(String.valueOf(key));

        do {
            result.append(DIGITS[value.mod(NUM_DIGITS).intValue()]);
            value = value.divide(NUM_DIGITS);
        } while (value.compareTo(BigInteger.ZERO) > 0);

        // Could have done .reverse() but the endiannes does not matter here, so long as it is consistent between conversions.
        return String.valueOf(result);
    }

    public static <StatUnitId> StatUnitId getStatUnitId(final String urlShortKey) {
        BigInteger value = BigInteger.ZERO;

        char[] characters = urlShortKey.toCharArray();
        // the traversal order is reversed as a micro-optimization.
        for (int i = characters.length - 1; i >= 0; i--) {
            char observed = characters[i];
            value = value.multiply(NUM_DIGITS).add(VALUES.get(observed));
        }

        return (StatUnitId)value;
    }

    @Data
    private static final class ShortenedEntry<UserId extends Number> {
        private final UserId owner;
        private final String originalUrl;
        private final RedirectType redirectType;
        private final AtomicLong visits = new AtomicLong();
    }

    static {
        VALUES = new HashMap<>();
        BigInteger value = BigInteger.ZERO;
        for (char DIGIT : DIGITS) {
            VALUES.put(DIGIT, value);
            value = value.add(BigInteger.ONE);
        }
    }

    // StatUnitId (short URL key) to original URL mapping
    private final Map<StatUnitId, ShortenedEntry> urls = new ConcurrentHashMap<>();

    // Lengthy URL to shortened StatUnitId (short URL keys) mapping (for uniqueness checks on original URLs)
    private final Map<String, StatUnitId> lengthyUrlToShortenedId = new ConcurrentHashMap<>();

    // Reverse ownership info (UserAccount to a list of owned StatUnitId)
    private final Map<UserId, Queue<StatUnitId>> owned = new ConcurrentHashMap<>();

    private final StatUnitIdGenerator idGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlShortener.class);
}
