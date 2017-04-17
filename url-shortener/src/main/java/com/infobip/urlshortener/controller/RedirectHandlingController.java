package com.infobip.urlshortener.controller;

import com.infobip.urlshortener.model.RedirectType;
import com.infobip.urlshortener.model.UrlShortener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

@RestController
@RequestMapping(method = RequestMethod.GET, value = "/{shortKey:[a-zA-Z0-9]{1,11}\\b}")
public class RedirectHandlingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectHandlingController.class);

    @Inject
    public RedirectHandlingController(final UrlShortener<Number, BigInteger> urlShortener) {
        this.urlShortener = urlShortener;
    }

    @RequestMapping(method = RequestMethod.GET)
    public void get(@PathVariable("shortKey") String shortKey,
            final HttpServletResponse httpServletResponse) {

        BigInteger statUnitId = UrlShortener.getStatUnitId(shortKey);
        urlShortener.urlVisited(statUnitId);

        String fullUrl = urlShortener.getOriginalUrl(statUnitId);
        RedirectType redirectType = urlShortener.getRedirectType(statUnitId);

        httpServletResponse.setStatus(redirectType.getValue());
        httpServletResponse.setHeader("Location", fullUrl);
    }

    private final UrlShortener<Number, BigInteger> urlShortener;
}
