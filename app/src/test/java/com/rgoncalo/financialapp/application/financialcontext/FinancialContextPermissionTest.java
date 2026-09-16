package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.application.account.ListAccountsSummary;
import com.rgoncalo.financialapp.application.account.ListAccountsSummaryRequest;
import com.rgoncalo.financialapp.application.security.AccessDeniedException;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.TestUsers;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class
})
class FinancialContextPermissionTest {

    @TestTemplate
    void ownerCanGrantWriteAccessToAnotherUser(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextId contextId = new FinancialContextId("household");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(contextId, "Household")
        );
        UserId ownerId = TestUsers.create(configuration, "owner");
        UserId memberId = TestUsers.create(configuration, "member");
        TestUsers.grant(
                configuration,
                ownerId,
                contextId,
                FinancialContextPermission.OWNER
        );

        new GrantFinancialContextPermission(
                TestUsers.authorization(configuration),
                configuration.createFinancialContextRepository(),
                configuration.createFinancialContextPermissionRepository(),
                configuration.createUserRepository()
        ).execute(new GrantFinancialContextPermissionRequest(
                contextId,
                memberId,
                FinancialContextPermission.WRITE,
                ownerId
        ));

        FinancialContextAuthorization authorization =
                TestUsers.authorization(configuration);
        assertTrue(authorization.isPermitted(
                memberId,
                contextId,
                FinancialContextPermission.READ
        ));
        assertTrue(authorization.isPermitted(
                memberId,
                contextId,
                FinancialContextPermission.WRITE
        ));
        assertEquals(
                FinancialContextPermission.WRITE,
                configuration.createFinancialContextPermissionRepository()
                        .findByUserAndContext(memberId, contextId)
                        .orElseThrow()
                        .permission()
        );
    }

    @TestTemplate
    void readAccessCannotCreateAccounts(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextId contextId = new FinancialContextId("personal");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(contextId, "Personal")
        );
        UserId userId = TestUsers.create(configuration, "reader");
        TestUsers.grant(
                configuration,
                userId,
                contextId,
                FinancialContextPermission.READ
        );

        CreateAccount createAccount = new CreateAccount(
                configuration.createAccountRepository(),
                TestUsers.authorization(configuration)
        );

        assertThrows(AccessDeniedException.class, () -> createAccount.execute(
                new CreateAccountRequest(
                        "Checking",
                        new MonetaryValue(0.0),
                        contextId,
                        userId
                )
        ));
    }

    @TestTemplate
    void userWithoutContextPermissionCannotReadAccounts(
            RepositoryTestConfiguration configuration
    ) {
        FinancialContextId contextId = new FinancialContextId("private");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(contextId, "Private")
        );
        UserId userId = TestUsers.create(configuration, "outsider");
        ListAccountsSummary listAccounts = new ListAccountsSummary(
                configuration.createAccountRepository(),
                TestUsers.authorization(configuration)
        );

        assertThrows(AccessDeniedException.class, () -> listAccounts.execute(
                new ListAccountsSummaryRequest(contextId, userId)
        ));
    }
}
