package com.rgoncalo.financialapp.rest.auth;

import com.rgoncalo.financialapp.commondata.user.UserRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Stores an authenticated application user in the HTTP session.
 */
@Component
public class UserSessionAuthenticator {

    private final SecurityContextRepository securityContextRepository;

    public UserSessionAuthenticator(
            SecurityContextRepository securityContextRepository // spring automatically injects here
    ) {
        this.securityContextRepository = securityContextRepository;
    }

    public void authenticate(
            UserRecord user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.userId().userId(),
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_USER")
                );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
