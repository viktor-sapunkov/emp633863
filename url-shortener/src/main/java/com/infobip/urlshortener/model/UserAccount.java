package com.infobip.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccount<UserId> {
    public UserAccount() { }

    public UserAccount(final String accountId, final String password, final UserId userId) {
        this.accountId = accountId;
        this.password = password;
        this.userId = userId;
    }

    @JsonProperty("AccountId")
    private String accountId;

    private String password;

    @JsonIgnore
    private UserId userId;
}
