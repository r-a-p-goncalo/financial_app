package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;
import com.rgoncalo.financialapp.infrastructure.database.memory.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountsSummaryTest {

    @Test
    void returnsAvailableAccountSummaries() {

        InMemoryAccountRepository repository =
                new InMemoryAccountRepository();

        repository.save(
                new Account(
                        "account-1",
                        "Checking",
                        new MonetaryValue(
                                new BigDecimal("100.00")
                        )
                )
        );

        repository.save(
                new Account(
                        "account-2",
                        "Savings",
                        new MonetaryValue(
                                new BigDecimal("500.00")
                        )
                )
        );

        AccountsSummary accountsSummary =
                new AccountsSummary(repository);

        Collection<AccountRecord> result =
                accountsSummary.execute(
                        new AccountsSummaryRequest()
                );

        assertEquals(
                Set.of(
                        new AccountRecord(
                                "account-1",
                                "Checking",
                                null
                        ),
                        new AccountRecord(
                                "account-2",
                                "Savings",
                                null
                        )
                ),
                Set.copyOf(result)
        );
    }
}