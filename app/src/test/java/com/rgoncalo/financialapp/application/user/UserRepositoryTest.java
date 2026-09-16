package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class
})
class UserRepositoryTest {

    @TestTemplate
    void createsListsAndFindsUsers(RepositoryTestConfiguration configuration) {
        CreateUser createUser = new CreateUser(
                configuration.createUserRepository()
        );

        UserRecord created = createUser.execute(
                new CreateUserRequest("Alice")
        );

        assertNotNull(created.userId());
        assertEquals("Alice", created.name());
        assertEquals(
                created,
                configuration.createUserRepository().findById(created.userId())
                        .orElseThrow()
        );
        assertFalse(new ListUsersSummary(
                configuration.createUserRepository()
        ).execute(new ListUsersSummaryRequest()).isEmpty());
    }
}
