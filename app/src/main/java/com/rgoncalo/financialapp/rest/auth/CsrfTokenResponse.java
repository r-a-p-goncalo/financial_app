package com.rgoncalo.financialapp.rest.auth;

import org.springframework.security.web.csrf.CsrfToken;

/**
 * Tells a browser client which header carries the session CSRF token.
 */
public record CsrfTokenResponse(String headerName, String token) {

    public static CsrfTokenResponse from(CsrfToken token) {
        return new CsrfTokenResponse(token.getHeaderName(), token.getToken());
    }
}
