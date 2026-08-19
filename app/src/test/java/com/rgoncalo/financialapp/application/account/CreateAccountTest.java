package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(RepositoryTestExtension.class)
class CreateAccountTest {

    @TestTemplate
    void canCreateAccount(RepositoryTestConfiguration configuration) {

        RecordingAccountRepository repository = new RecordingAccountRepository(configuration.createAccountRepository());

        CreateAccount useCase = new CreateAccount(repository);

        String name = "Account";
        MonetaryValue initialAmount = new MonetaryValue(new BigDecimal("125.5"));
        String financialContextId = "context-1";

        AccountRecord result = useCase.execute(
                new CreateAccountRequest(
                        name,
                        initialAmount,
                        financialContextId
                )
        );

        assertEquals(1, repository.saveCalls());

        Optional<AccountRecord> getResult = repository.findById(result.id());

        assertFalse(getResult.isEmpty());

        AccountRecord saved = getResult.get();

        assertNotNull(saved);
        assertNotNull(saved.id());
        assertFalse(saved.id().isBlank());
        assertEquals(name, saved.name());
        assertEquals(initialAmount, saved.initial_value());
        assertEquals(financialContextId, saved.financialContextId());

        assertEquals(saved, result);
    }

}
