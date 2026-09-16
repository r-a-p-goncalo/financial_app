package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingTransactionRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class CreateTransactionTest {

    @TestTemplate
    void canCreateTransaction(
            RepositoryTestConfiguration configuration
    ) {

        AccountRepository accountRepository =
                configuration.createAccountRepository();

        RecordingTransactionRepository transactionRepository =
                new RecordingTransactionRepository(
                        configuration.createTransactionRepository()
                );

        FinancialContextId context =
                new FinancialContextId(
                        "context-1"
                );
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(context, "Personal")
        );
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, context,
                FinancialContextPermission.WRITE);

        AccountRecord origin =
                new AccountRecord(
                        new AccountRecordId(
                                "origin-account",
                                context
                        ),
                        "Origin",
                        new MonetaryValue(
                                new BigDecimal("1000")
                        )
                );

        AccountRecord target =
                new AccountRecord(
                        new AccountRecordId(
                                "target-account",
                                context
                        ),
                        "Target",
                        new MonetaryValue(
                                new BigDecimal("500")
                        )
                );

        accountRepository.save(origin);
        accountRepository.save(target);

        CreateTransaction useCase =
                new CreateTransaction(
                        transactionRepository,
                        accountRepository,
                        TestUsers.authorization(configuration)
                );

        Instant dateTime =
                Instant.parse(
                        "2026-08-21T10:00:00Z"
                );

        MonetaryValue value =
                new MonetaryValue(
                        new BigDecimal("125.50")
                );

        TransactionRecord result =
                useCase.execute(
                        new CreateTransactionRequest(
                                context,
                                origin.accountRecordId(),
                                target.accountRecordId(),
                                dateTime,
                                value,
                                userId
                        )
                );

        assertEquals(
                1,
                transactionRepository.saveCalls()
        );

        assertNotNull(result);

        assertNotNull(result.transactionRecordId());

        assertNotNull(
                result.transactionRecordId().transactionRecordId()
        );

        assertEquals(
                context,
                result.transactionRecordId().financialContextId()
        );

        assertEquals(
                origin.accountRecordId(),
                result.originAccountId()
        );

        assertEquals(
                target.accountRecordId(),
                result.targetAccountId()
        );

        assertEquals(
                dateTime,
                result.dateTime()
        );

        assertEquals(
                value,
                result.value()
        );

        Optional<TransactionRecord> saved =
                transactionRepository.findById(
                        result.transactionRecordId()
                );

        assertTrue(saved.isPresent());

        assertEquals(
                result,
                saved.get()
        );
    }

    @TestTemplate
    void canCreateTransactionWithOneExternalSide(
            RepositoryTestConfiguration configuration
    ) {

        AccountRepository accountRepository =
                configuration.createAccountRepository();
        TransactionRepository transactionRepository =
                configuration.createTransactionRepository();

        FinancialContextId context =
                new FinancialContextId("context-1");
        configuration.createFinancialContextRepository().save(
                new FinancialContextRecord(context, "Personal")
        );
        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, context,
                FinancialContextPermission.WRITE);
        AccountRecord origin =
                new AccountRecord(
                        new AccountRecordId("origin-account", context),
                        "Origin",
                        new MonetaryValue(new BigDecimal("1000"))
                );

        accountRepository.save(origin);

        TransactionRecord result = new CreateTransaction(
                transactionRepository,
                accountRepository,
                TestUsers.authorization(configuration)
        ).execute(
                new CreateTransactionRequest(
                        context,
                        origin.accountRecordId(),
                        null,
                        Instant.parse("2026-08-21T10:00:00Z"),
                        new MonetaryValue(new BigDecimal("125.50")),
                        userId
                )
        );

        assertEquals(origin.accountRecordId(), result.originAccountId());
        assertNull(result.targetAccountId());
        assertEquals(
                result,
                transactionRepository.findById(
                        result.transactionRecordId()
                ).orElseThrow()
        );
    }
}
