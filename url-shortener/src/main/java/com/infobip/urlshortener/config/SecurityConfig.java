package com.infobip.urlshortener.config;

import com.infobip.urlshortener.security.UrlShortenersAuthenticationProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.infobip.urlshortener.security")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(urlShortenersAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/help.html", "/css/**", "/img/**", "/js/**").anonymous()
            .and()
                .authorizeRequests().mvcMatchers(HttpMethod.GET, "/{shortKey:[a-zA-Z0-9]{1,11}\\b}").anonymous()
            .and()
                .authorizeRequests().mvcMatchers(HttpMethod.POST, "/account").anonymous()
            .and()
                .authorizeRequests().mvcMatchers(HttpMethod.POST, "/register").authenticated()
            .and()
                .authorizeRequests().mvcMatchers(HttpMethod.GET, "/statistic/{AccountId}").authenticated()
            .and()
                .httpBasic();
    }

    @Resource
    private UrlShortenersAuthenticationProvider urlShortenersAuthenticationProvider;
}
