package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.application.security.AuthenticationException;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

/**
 * Authenticates a user without exposing password handling to the client.
 */
public class AuthenticateUser {

    private final UserRepository userRepository;
    private final PasswordHashingStrategyResolver passwordHashingStrategies;

    public AuthenticateUser(
            UserRepository userRepository,
            PasswordHashingStrategyResolver passwordHashingStrategies
    ) {
        this.userRepository = userRepository;
        this.passwordHashingStrategies = passwordHashingStrategies;
    }

    public UserRecord execute(AuthenticateUserRequest request) {
        if (request.name() == null || request.name().isBlank()
                || request.password() == null) {
            throw new AuthenticationException();
        }

        UserRecord user = userRepository.findByName(request.name().trim())
                .orElseThrow(AuthenticationException::new);

        if (user.passwordHash() == null) {
            throw new AuthenticationException();
        }

        boolean matches = passwordHashingStrategies.findById(
                        user.passwordHash().strategyId()
                )
                .map(strategy -> strategy.matches(
                        request.password(),
                        user.passwordHash()
                ))
                .orElse(false);

        if (!matches) {
            throw new AuthenticationException();
        }

        return user;
    }
}
