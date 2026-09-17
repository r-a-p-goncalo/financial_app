package com.rgoncalo.financialapp.rest.auth;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.user.AuthenticateUserRequest;
import com.rgoncalo.financialapp.application.user.CreateUserRequest;
import com.rgoncalo.financialapp.application.user.GetUserRequest;
import com.rgoncalo.financialapp.commondata.user.UserRecord;
import com.rgoncalo.financialapp.rest.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Browser-session registration and authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final Application application;
    private final CurrentUser currentUser;
    private final UserSessionAuthenticator sessionAuthenticator;

    public AuthenticationController(
            Application application,
            CurrentUser currentUser,
            UserSessionAuthenticator sessionAuthenticator
    ) {
        this.application = application;
        this.currentUser = currentUser;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(
                CsrfToken.class.getName()
        );

        if (token == null) {
            throw new IllegalStateException("Could not create CSRF token.");
        }

        return CsrfTokenResponse.from(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        UserRecord user = application.createUser().execute(
                new CreateUserRequest(request.name(), request.password())
        );
        sessionAuthenticator.authenticate(user, servletRequest, servletResponse);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        UserRecord user = application.authenticateUser().execute(
                new AuthenticateUserRequest(request.name(), request.password())
        );
        sessionAuthenticator.authenticate(user, servletRequest, servletResponse);

        return UserResponse.from(user);
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        UserRecord user = application.getUserById().execute(
                new GetUserRequest(currentUser.userId(authentication))
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user does not exist."
        ));

        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
