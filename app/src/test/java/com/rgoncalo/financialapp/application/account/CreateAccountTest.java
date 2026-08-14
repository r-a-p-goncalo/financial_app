package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;
import com.rgoncalo.financialapp.support.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreateAccountTest {

    private InMemoryAccountRepository repository;
    private CreateAccount createAccount;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
        createAccount = new CreateAccount(repository);
    }

    @Test
    void createsAccountWithRequestedValues() {

        MonetaryValue initialAmount =
                new MonetaryValue(new BigDecimal("125.50"));

        CreateAccountRequest request =
                new CreateAccountRequest(
                        "Checking account",
                        initialAmount
                );

        Account account = createAccount.execute(request);

        assertEquals(
                "Checking account",
                account.getName()
        );

        assertEquals(
                new BigDecimal("125.50"),
                account.getInitialAmount().getValue()
        );

        assertNotNull(account.getId());
        assertFalse(account.getId().isBlank());
    }

    @Test
    void persistsCreatedAccount() {

        CreateAccountRequest request =
                new CreateAccountRequest(
                        "Savings",
                        new MonetaryValue(
                                new BigDecimal("500.00")
                        )
                );

        Account created = createAccount.execute(request);

        assertEquals(1, repository.size());

        Account stored = repository
                .findById(created.getId())
                .orElseThrow();

        assertEquals(created.getId(), stored.getId());
        assertEquals(created.getName(), stored.getName());

        assertEquals(
                0,
                created.getInitialAmount()
                        .getValue()
                        .compareTo(
                                stored.getInitialAmount().getValue()
                        )
        );
    }
}