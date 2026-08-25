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
        TestLoggingExtension.class
})
class ListTransactionsSummaryForAccountTest {

    @TestTemplate
    void returnsIncomingAndOutgoingTransactionsOnly(
            RepositoryTestConfiguration configuration
    ) {

        TransactionRepository repository =
                configuration.createTransactionRepository();

        FinancialContextId context1 = new FinancialContextId("context-1");
        FinancialContextId context2 = new FinancialContextId("context-2");
        AccountRecordId account = new AccountRecordId("account", context1);
        AccountRecordId other = new AccountRecordId("other", context1);
        AccountRecordId sameAccountInOtherContext = new AccountRecordId(
                "account",
                context2
        );

        TransactionRecord outgoing = transaction(
                "outgoing",
                context1,
                account,
                other,
                "10"
        );
        TransactionRecord incoming = transaction(
                "incoming",
                context1,
                other,
                account,
                "20"
        );
        TransactionRecord unrelated = transaction(
                "unrelated",
                context1,
                other,
                new AccountRecordId("another", context1),
                "30"
        );
        TransactionRecord otherContext = transaction(
                "other-context",
                context2,
                sameAccountInOtherContext,
                new AccountRecordId("other", context2),
                "40"
        );

        repository.save(outgoing);
        repository.save(incoming);
        repository.save(unrelated);
        repository.save(otherContext);

        Collection<TransactionRecord> result =
                new ListTransactionsSummaryForAccount(repository).execute(
                        new ListTransactionsSummaryForAccountRequest(account)
                );

        assertEquals(2, result.size());
        assertTrue(result.contains(outgoing));
        assertTrue(result.contains(incoming));
    }

    private TransactionRecord transaction(
            String transactionId,
            FinancialContextId context,
            AccountRecordId origin,
            AccountRecordId target,
            String value
    ) {

        return new TransactionRecord(
                new TransactionRecordId(transactionId, context),
                origin,
                target,
                Instant.parse("2026-01-01T00:00:00Z"),
                new MonetaryValue(new BigDecimal(value))
        );
    }
}
