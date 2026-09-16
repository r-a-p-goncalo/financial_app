package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})
class CreateAccountTest {

    @TestTemplate
    void canCreateAccount(RepositoryTestConfiguration configuration) {

        RecordingAccountRepository repository = new RecordingAccountRepository(configuration.createAccountRepository());

        String name = "Account";
        MonetaryValue initialAmount = new MonetaryValue(new BigDecimal("125.5"));
        FinancialContextId context1 = new FinancialContextId("context-1");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(context1, "Personal")
        );
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, context1,
                FinancialContextPermission.WRITE);
        CreateAccount useCase = new CreateAccount(
                repository,
                TestUsers.authorization(configuration)
        );

        AccountRecord result = useCase.execute(
                new CreateAccountRequest(
                        name,
                        initialAmount,
                        context1,
                        userId
                )
        );

        assertEquals(1, repository.saveCalls());

        Optional<AccountRecord> getResult = repository.findById(result.accountRecordId());

        assertFalse(getResult.isEmpty());

        AccountRecord saved = getResult.get();

        assertNotNull(saved);
        assertNotNull(saved.accountRecordId());
        assertEquals(name, saved.name());
        assertEquals(initialAmount, saved.initialAmount());
        assertEquals(context1, saved.accountRecordId().financialContextId());

        assertEquals(saved, result);
    }

}
