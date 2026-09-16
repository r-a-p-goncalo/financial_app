package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserRecord;
import com.rgoncalo.financialapp.application.security.AuthenticationException;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.infrastructure.security.PasswordHashingStrategyRegistry;
import com.rgoncalo.financialapp.infrastructure.security.Pbkdf2PasswordHashingStrategy;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class
})
class UserRepositoryTest {

    @TestTemplate
    void createsListsAndFindsUsers(RepositoryTestConfiguration configuration) {
        PasswordHashingStrategyRegistry passwordHashingStrategies =
                new PasswordHashingStrategyRegistry(
                        new Pbkdf2PasswordHashingStrategy()
                );
        CreateUser createUser = new CreateUser(
                configuration.createUserRepository(),
                passwordHashingStrategies
        );

        UserRecord created = createUser.execute(
                new CreateUserRequest("Alice", "correct password")
        );

        assertNotNull(created.userId());
        assertEquals("Alice", created.name());
        assertNotNull(created.passwordHash());
        assertFalse(created.passwordHash().value().contains(
                "correct password"
        ));
        assertEquals(
                created,
                configuration.createUserRepository().findById(created.userId())
                        .orElseThrow()
        );
        assertFalse(new ListUsersSummary(
                configuration.createUserRepository()
        ).execute(new ListUsersSummaryRequest()).isEmpty());

        AuthenticateUser authenticateUser = new AuthenticateUser(
                configuration.createUserRepository(),
                passwordHashingStrategies
        );
        assertEquals(
                created,
                authenticateUser.execute(new AuthenticateUserRequest(
                        "Alice",
                        "correct password"
                ))
        );
        assertThrows(AuthenticationException.class, () ->
                authenticateUser.execute(new AuthenticateUserRequest(
                        "Alice",
                        "wrong password"
                ))
        );
        assertThrows(IllegalArgumentException.class, () ->
                createUser.execute(new CreateUserRequest(
                        "Alice",
                        "another password"
                ))
        );
    }
}
