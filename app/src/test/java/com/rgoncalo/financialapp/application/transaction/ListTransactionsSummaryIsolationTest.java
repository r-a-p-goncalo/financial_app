package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class ListTransactionsSummaryIsolationTest {

    @TestTemplate
    void onlyReturnsTransactionsFromRequestedFinancialContext(
            RepositoryTestConfiguration configuration
    ) {


        TransactionRepository repository =
                configuration.createTransactionRepository();

        FinancialContextId context1 =
                new FinancialContextId(
                        "context-1"
                );

        FinancialContextId context2 =
                new FinancialContextId(
                        "context-2"
                );

        AccountRecordId account1Id = new AccountRecordId("account-1", context1);
        AccountRecordId account2Id = new AccountRecordId("account-2", context1);

        AccountRecordId account3Id = new AccountRecordId("account-3", context2);
        AccountRecordId account4Id = new AccountRecordId("account-4", context2);


        TransactionRecord transaction1 =
                new TransactionRecord(
                        new TransactionRecordId(
                                "transaction-1",
                                context1
                        ),
                        account1Id,
                        account2Id,
                        Instant.parse(
                                "2026-01-01T00:00:00Z"
                        ),
                        new MonetaryValue(
                                new BigDecimal("100")
                        )
                );

        TransactionRecord transaction2 =
                new TransactionRecord(
                        new TransactionRecordId(
                                "transaction-2",
                                context2
                        ),
                        account3Id,
                        account4Id,
                        Instant.parse(
                                "2026-01-02T00:00:00Z"
                        ),
                        new MonetaryValue(
                                new BigDecimal("200")
                        )
                );

        repository.save(transaction1);
        repository.save(transaction2);

        ListTransactionsSummary useCase =
                new ListTransactionsSummary(
                        repository
                );

        Collection<TransactionRecord> result =
                useCase.execute(
                        new ListTransactionsSummaryRequest(
                                context1
                        )
                );

        assertEquals(
                1,
                result.size()
        );

        assertTrue(
                result.contains(
                        transaction1
                )
        );
    }
}