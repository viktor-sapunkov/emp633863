package com.infobip.urlshortener.model.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.ACCEPTED, reason = "That account has been taken")
public class AccountTakenException extends RuntimeException {
}
