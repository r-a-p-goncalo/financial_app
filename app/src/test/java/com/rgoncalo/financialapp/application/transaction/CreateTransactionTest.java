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
                        accountRepository
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
                                value
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
}