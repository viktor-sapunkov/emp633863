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
import java.util.Collections;
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

        Optional<Number> userId = ofNullable(crowd.getUserByAccountId(accountId)).map(UserAccount::getUserId);
        if (!userId.isPresent()) {
            return Collections.emptyMap();
        }

        if (!userId.equals(ofNullable(crowd.getUserByAccountId(principal.getName())).map(UserAccount::getUserId))) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Collections.emptyMap();
        }

        Queue<BigInteger> allMatchingShortkeys = urlShortener.getAllShortKeysByUserId(userId.get());
        Map<?, ?> urlToHits = allMatchingShortkeys.stream()
                .collect(Collectors.toMap(urlShortener::getOriginalUrl, urlShortener::getHits));

        return urlToHits;
    }

    private final Crowd<Number> crowd;
    private final UrlShortener<Number, BigInteger> urlShortener;
}
