package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.*;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class ListAccountsSummaryIsolationTest {

    @TestTemplate
    void accountsFromAnotherFinancialContextAreNotVisible(RepositoryTestConfiguration configuration) {
        AccountRepository repository = configuration.createAccountRepository();

        FinancialContextId personalFinancialContextId = new FinancialContextId("personal");
        FinancialContextId businessFinancialContextId = new FinancialContextId("business");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(personalFinancialContextId, "Personal")
        );
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(businessFinancialContextId, "Business")
        );
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, personalFinancialContextId,
                FinancialContextPermission.WRITE);
        TestUsers.grant(configuration, userId, businessFinancialContextId,
                FinancialContextPermission.WRITE);
        CreateAccount createAccount = new CreateAccount(
                repository,
                TestUsers.authorization(configuration)
        );
        ListAccountsSummary listAccountsSummary = new ListAccountsSummary(
                repository,
                TestUsers.authorization(configuration)
        );

        createAccount.execute(new CreateAccountRequest(
                "Personal Checking",
                new MonetaryValue(new BigDecimal("1000.00")),
                personalFinancialContextId,
                userId
        ));

        createAccount.execute(new CreateAccountRequest(
                "Personal Savings",
                new MonetaryValue(new BigDecimal("5000.00")),
                personalFinancialContextId,
                userId
        ));

        createAccount.execute(new CreateAccountRequest(
                "Business Checking",
                new MonetaryValue(new BigDecimal("10000.00")),
                businessFinancialContextId,
                userId
        ));

        Collection<AccountRecord> personalAccounts =
                listAccountsSummary.execute(
                        new ListAccountsSummaryRequest(
                                personalFinancialContextId,
                                userId
                        )
                );

        Collection<AccountRecord> businessAccounts =
                listAccountsSummary.execute(
                        new ListAccountsSummaryRequest(
                                businessFinancialContextId,
                                userId
                        )
                );

        assertEquals(2, personalAccounts.size());
        assertTrue(personalAccounts.stream()
                .allMatch(account ->
                        account.accountRecordId().financialContextId().equals(personalFinancialContextId)));

        assertEquals(1, businessAccounts.size());
        assertTrue(businessAccounts.stream()
                .allMatch(account ->
                        account.accountRecordId().financialContextId().equals(businessFinancialContextId)));

        assertTrue(personalAccounts.stream()
                .noneMatch(account ->
                        account.name().equals("Business Checking")));

        assertTrue(businessAccounts.stream()
                .noneMatch(account ->
                        account.name().equals("Personal Checking")));

        assertTrue(businessAccounts.stream()
                .noneMatch(account ->
                        account.name().equals("Personal Savings")));
    }
}
