package com.infobip.urlshortener.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infobip.urlshortener.model.Crowd;
import com.infobip.urlshortener.model.RedirectType;
import com.infobip.urlshortener.model.UrlShortener;
import com.infobip.urlshortener.model.UserAccount;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ejb.DependsOn;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.security.Principal;

import static java.util.Optional.ofNullable;

@DependsOn({"AccountController", "StatisticsController"})       // those are to have precedence
@RestController
@RequestMapping(method = RequestMethod.POST, value = "/register")
public class UrlsRegistryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsRegistryController.class);

    @Inject
    public UrlsRegistryController(
            final Crowd<Number> crowd,
            final UrlShortener<Number, BigInteger> urlShortener) {

        this.crowd = crowd;
        this.urlShortener = urlShortener;
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Output get(
            @Valid @RequestBody Input input,
            BindingResult bindingResult,
            Principal principal,
            final HttpServletResponse httpServletResponse,
            final HttpServletRequest httpServletRequest,
            final UriComponentsBuilder uriComponentsBuilder) throws BindException {

        final int[] httpStatusHolder = {HttpServletResponse.SC_CREATED};

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String username = principal.getName();
        Number userId = ofNullable(crowd.getUserByAccountId(username))
                            .map(UserAccount::getUserId)
                            .orElseThrow(() -> new IllegalStateException(String.format("Unknown user %s", username)));

        Number urlShortKey = urlShortener.shorten(
                userId, input.getUrl(), ofNullable(input.getRedirectType()).orElse(RedirectType.FOUND),
                () -> httpStatusHolder[0] = HttpServletResponse.SC_ACCEPTED);

        httpServletResponse.setStatus(httpStatusHolder[0]);
        String url = uriComponentsBuilder.path(UrlShortener.getShortenedUrl(urlShortKey)).toUriString();
        return new Output(url);
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class Input {
        @NotNull
        @Length(min = 1, max = 1048576)
        String url;

        RedirectType redirectType;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static final class Output {
        final String shortUrl;
    }

    private final Crowd<Number> crowd;
    private final UrlShortener<Number, BigInteger> urlShortener;
}
