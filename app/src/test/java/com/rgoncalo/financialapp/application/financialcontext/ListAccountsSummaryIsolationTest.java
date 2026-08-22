package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.*;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
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

        CreateAccount createAccount = new CreateAccount(repository);
        ListAccountsSummary listAccountsSummary = new ListAccountsSummary(repository);

        FinancialContextId personalFinancialContextId = new FinancialContextId("personal");
        FinancialContextId businessFinancialContextId = new FinancialContextId("business");

        createAccount.execute(new CreateAccountRequest(
                "Personal Checking",
                new MonetaryValue(new BigDecimal("1000.00")),
                personalFinancialContextId
        ));

        createAccount.execute(new CreateAccountRequest(
                "Personal Savings",
                new MonetaryValue(new BigDecimal("5000.00")),
                personalFinancialContextId
        ));

        createAccount.execute(new CreateAccountRequest(
                "Business Checking",
                new MonetaryValue(new BigDecimal("10000.00")),
                businessFinancialContextId
        ));

        Collection<AccountRecord> personalAccounts =
                listAccountsSummary.execute(
                        new ListAccountsSummaryRequest(personalFinancialContextId)
                );

        Collection<AccountRecord> businessAccounts =
                listAccountsSummary.execute(
                        new ListAccountsSummaryRequest(businessFinancialContextId)
                );

        assertEquals(2, personalAccounts.size());
        assertTrue(personalAccounts.stream()
                .allMatch(account ->
                        account.id().financialContextId().equals(personalFinancialContextId)));

        assertEquals(1, businessAccounts.size());
        assertTrue(businessAccounts.stream()
                .allMatch(account ->
                        account.id().financialContextId().equals(businessFinancialContextId)));

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