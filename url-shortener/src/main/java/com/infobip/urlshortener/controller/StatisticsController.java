package com.infobip.urlshortener.controller;

import com.infobip.urlshortener.model.Crowd;
import com.infobip.urlshortener.model.UrlShortener;
import com.infobip.urlshortener.model.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(method = RequestMethod.GET, value = "/statistic")
public class StatisticsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Inject
    public StatisticsController(
            final Crowd<Number> crowd,
            final UrlShortener<Number, BigInteger> urlShortener) {

        this.crowd = crowd;
        this.urlShortener = urlShortener;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{accountId}")
    public @ResponseBody Map<?, ?> get(
            @PathVariable("accountId") String accountId,
            Principal principal,
            HttpServletResponse httpServletResponse) {

        Optional<Number> principalUserId = ofNullable(crowd.getUserByAccountId(principal.getName()))
                .map(UserAccount::getUserId);
        boolean notAuthorized = !principalUserId.isPresent();

        Optional<Number> requestedUserId = ofNullable(crowd.getUserByAccountId(accountId))
                .map(UserAccount::getUserId);
        notAuthorized |= !requestedUserId.isPresent();

        if (notAuthorized || !requestedUserId.get().equals(principalUserId.get())) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        Number userId = requestedUserId.get();
        Queue<BigInteger> allMatchingShortkeys = urlShortener.getAllShortKeysByUserId(userId);
        Map<?, ?> urlToHits = allMatchingShortkeys.stream()
                .collect(Collectors.toMap(urlShortener::getOriginalUrl, urlShortener::getHits));

        return urlToHits;
    }

    private final Crowd<Number> crowd;
    private final UrlShortener<Number, BigInteger> urlShortener;
}
