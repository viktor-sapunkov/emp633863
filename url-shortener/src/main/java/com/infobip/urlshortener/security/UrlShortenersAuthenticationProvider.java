package com.infobip.urlshortener.security;

import com.infobip.urlshortener.model.Crowd;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;

@Component
public class UrlShortenersAuthenticationProvider implements AuthenticationProvider {

    @Inject
    public UrlShortenersAuthenticationProvider(final Crowd crowd) {
        this.crowd = crowd;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        if (!crowd.hasAccountId(name)) {
            throw new UsernameNotFoundException(String.format("Authentication failed for user %s (user not found)", name));
        } else if (!password.equals(crowd.getUserByAccountId(name).getPassword())) {
            throw new BadCredentialsException(String.format("Authentication failed for user %s (wrong password)", name));
        }

        return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private final Crowd crowd;
}
