package com.infobip.urlshortener.model;

import com.infobip.urlshortener.URLShortenerApplication;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class Crowd<UserId extends Number> {
    public static final int PW_LEN = 8;
    public static final char[] PW_CHARS = URLShortenerApplication.ALPHANUMERIC;

    @Inject
    public Crowd(final UserIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public UserAccount<UserId> getUserByUserId(final UserId userId) {
        return users.get(userId);
    }

    public UserAccount<UserId> getUserByAccountId(final String accountId) {
        return byAccountId.get(accountId);
    }

    public boolean hasUserId(final UserId userId) {
        return users.containsKey(userId);
    }

    public boolean hasAccountId(final String accountId) {
        return byAccountId.containsKey(accountId);
    }

    public UserId newUser(final UserAccount<UserId> userAccount) {
        if (hasAccountId(userAccount.getAccountId())) {
            return null;
        }

        String password = userAccount.getPassword();
        if (password == null) {
            final StringBuilder pwgen = new StringBuilder();
            for (int i = 0; i < PW_LEN; i++) {
                int random = idGenerator.get().intValue();
                random *= random;       // not random enough
                random %= PW_CHARS.length;

                if (random < 0) {
                    random += PW_CHARS.length;
                }

                pwgen.append(PW_CHARS[random]);
            }
            password = String.valueOf(pwgen);
        }

        UserAccount<UserId> created;
        for (;;) {
            UserId userId = (UserId)idGenerator.get();
            created = new UserAccount<>(userAccount.getAccountId(), password, userId);
            if (users.putIfAbsent(userId, created) == null) {
                byAccountId.put(created.getAccountId(), created);
                break;
            }
        }

        return created.getUserId();
    }

    private final UserIdGenerator idGenerator;

    private final Map<UserId, UserAccount<UserId>> users = new ConcurrentHashMap<>();
    private final Map<String, UserAccount<UserId>> byAccountId = new ConcurrentHashMap<>();
}
