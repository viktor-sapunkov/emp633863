package com.infobip.urlshortener.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infobip.urlshortener.model.Crowd;
import com.infobip.urlshortener.model.UserAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(method = RequestMethod.POST, value = "/account")
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Inject
    public AccountController(Crowd<Number> crowd) {
        this.crowd = crowd;
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody UserAccount newAccount(
            @Valid @RequestBody
            final UserAccount<?> newUserAccount, final BindingResult bindingResult,
            final HttpServletResponse httpServletResponse) throws BindException {

        UserAccount userAccount = null;
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String accountId = newUserAccount.getAccountId();
        boolean taken = crowd.hasAccountId(accountId);
        if (!taken) {
            Number userId = crowd.newUser(new UserAccount<>(accountId, null, null));
            userAccount = crowd.getUserByUserId(userId);

            taken = userAccount == null;        // check for race conditions
        }

        if (taken) {
            userAccount = new UserAccountWithCreationFeedback(accountId, false);
            httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
        }

        Optional<String> password = ofNullable(userAccount).map(UserAccount::getPassword);
        if (password.isPresent()) {
            userAccount = new UserAccountWithCreationFeedback(accountId, true);
            userAccount.setPassword(password.get());
            httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
        }
        return userAccount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class UserAccountWithCreationFeedback extends UserAccount<Number> {
        UserAccountWithCreationFeedback(final String accountId, boolean success) {
            this.setAccountId(accountId);
            this.success = success;
            this.description = success
                             ? String.format("Account ID %s has been provisioned.", accountId)
                             : String.format("Account ID %s has already been taken.", accountId);
        }

        boolean success = false;
        String description;
    }

    private final Crowd<Number> crowd;
}
