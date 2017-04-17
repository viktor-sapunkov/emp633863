package com.infobip.urlshortener.model;

import com.infobip.urlshortener.URLShortenerApplication;
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
public class UrlShortener<UserId extends Number, StatUnitId extends BigInteger> {
    public static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            new String[]{"register", "account", "statistic", "favicon", "help", "css", "img", "js"}));

    public static final char[] DIGITS = URLShortenerApplication.ALPHANUMERIC;
    public static final BigInteger NUM_DIGITS = new BigInteger(String.valueOf(DIGITS.length));
    public static final Map<Character, BigInteger> VALUES;

    @Inject
    public UrlShortener(final Crowd crowd, final StatUnitIdGenerator idGenerator) {
        this.crowd = crowd;
        this.idGenerator = idGenerator;
    }

    public StatUnitId shorten(final UserId subject, final String url, final RedirectType redirectType,
                              final Runnable collisionsHandler) {

        Optional<StatUnitId> shortened = ofNullable(keys.get(url));
        if (shortened.isPresent()) {
            collisionsHandler.run();
            return shortened.get();
        }

        for (;;) {
            shortened = Optional.of((StatUnitId)idGenerator.get());
            StatUnitId key = shortened.get();

            if (stopWordsMatch(key)) {
                continue;
            }

            if (urls.putIfAbsent(key, url) == null) {
                this.redirectType.putIfAbsent(key, redirectType);
                keys.put(url, key);
                stats.put(key, new AtomicLong());
                owner.put(key, subject);

                if (owned.putIfAbsent(subject, new ConcurrentLinkedQueue<>(Collections.singletonList(key))) != null) {
                    owned.get(subject).add(key);
                }
                break;
            }
        }
        return shortened.get();
    }

    public boolean stopWordsMatch(StatUnitId key) {
        String converted = getShortenedUrl(key);
        return STOP_WORDS.contains(converted);
    }

    public void urlVisited(final StatUnitId urlId) {
        if (stats.putIfAbsent(urlId, new AtomicLong()) != null) {
            stats.get(urlId).incrementAndGet();
        }
    }

    public long getHits(final StatUnitId urlId) {
        return ofNullable(stats.get(urlId)).map(AtomicLong::longValue).orElseGet(() -> 0L);
    }

    public Queue<StatUnitId> getAllShortKeysByUserId(final UserId userId) {
        return ofNullable(owned.get(userId)).orElseGet(ArrayDeque::new);
    }

    public String getOriginalUrl(final StatUnitId url) {
        return ofNullable(urls.get(url))
            .orElseThrow(() -> new UnknownUrlException(String.format("%s is unknown short URL", getShortenedUrl(url))));
    }

    public RedirectType getRedirectType(final StatUnitId url) {
        return ofNullable(redirectType.get(url))
            .orElseThrow(() -> new UnknownUrlException(String.format("%s is unknown short URL", getShortenedUrl(url))));
    }

    public static <StatUnitId> String getShortenedUrl(final StatUnitId key) {
        final StringBuilder result = new StringBuilder();
        BigInteger value = new BigInteger(String.valueOf(key));

        while (value.compareTo(BigInteger.ZERO) > 0) {
            result.append(DIGITS[value.mod(NUM_DIGITS).intValue()]);
            value = value.divide(NUM_DIGITS);
        }

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

    static {
        VALUES = new HashMap<>();
        BigInteger value = BigInteger.ZERO;
        for (char DIGIT : DIGITS) {
            VALUES.put(DIGIT, value);
            value = value.add(BigInteger.ONE);
        }
    }

    // StatUnitId (short URL key) to original URL mapping
    private final Map<StatUnitId, String> urls = new ConcurrentHashMap<>();

    private final Map<StatUnitId, RedirectType> redirectType = new ConcurrentHashMap<>();

    // Lengthy URL to StatUnitId (short URL keys) mapping (for uniqueness checks)
    private final Map<String, StatUnitId> keys = new ConcurrentHashMap<>();

    // Statistics of visits - shared between users
    private final Map<StatUnitId, AtomicLong> stats = new ConcurrentHashMap<>();

    // Ownership info (UserAccount per shortened URL key)
    private final Map<StatUnitId, UserId> owner = new ConcurrentHashMap<>();

    // Reverse ownership info (UserAccount to a list of owned StatUnitId)
    private final Map<UserId, Queue<StatUnitId>> owned = new ConcurrentHashMap<>();

    // Crowd (the entire userbase)
    private final Crowd crowd;

    private final StatUnitIdGenerator idGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlShortener.class);
}
