package com.rgoncalo.financialapp.rest;

import com.rgoncalo.financialapp.commondata.user.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Resolves the financial-app identity from Spring Security authentication.
 */
@Component
public class CurrentUser {

    public UserId userId(Authentication authentication) {
        return new UserId(authentication.getName());
    }
}
