package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CreateAccountTest {

    @Test
    void createsAndPersistsAccountFromRequest() {
        RecordingAccountRepository repository = new RecordingAccountRepository();
        CreateAccount useCase = new CreateAccount(repository);
        MonetaryValue initialAmount = new MonetaryValue(new BigDecimal("125.50"));

        AccountRecord result = useCase.execute(
                new CreateAccountRequest(
                        "Checking",
                        initialAmount,
                        "context-1"
                )
        );

        assertEquals(1, repository.saveCalls());

        AccountRecord saved = repository.savedAccount();
        assertNotNull(saved);
        assertNotNull(saved.id());
        assertFalse(saved.id().isBlank());
        assertEquals("Checking", saved.name());
        assertSame(initialAmount, saved.initial_value());
        assertEquals("context-1", saved.financialContextId());

        assertSame(saved, result);
    }
}
