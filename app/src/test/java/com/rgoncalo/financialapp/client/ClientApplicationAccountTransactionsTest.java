package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.client.data.account.AccountTransactionSummary;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientApplicationAccountTransactionsTest {

    @Test
    void loadsAnAccountAndReturnsItsRunningTransactionTotals() {

        FinancialContextId contextId = new FinancialContextId("context");
        AccountRecordId accountId = new AccountRecordId("account", contextId);
        AccountRecordId otherAccountId = new AccountRecordId("other", contextId);
        InMemoryAccountRepository accountRepository =
                new InMemoryAccountRepository();
        InMemoryFinancialContextRepository contextRepository =
                new InMemoryFinancialContextRepository();
        InMemoryTransactionRepository transactionRepository =
                new InMemoryTransactionRepository();

        contextRepository.save(
                new FinancialContextRecord(contextId, "Personal")
        );
        accountRepository.save(
                new AccountRecord(
                        accountId,
                        "Checking",
                        monetaryValue("100")
                )
        );
        accountRepository.save(
                new AccountRecord(
                        otherAccountId,
                        "Savings",
                        monetaryValue("0")
                )
        );
        transactionRepository.save(
                transaction(
                        "incoming",
                        contextId,
                        otherAccountId,
                        accountId,
                        "2026-01-02T00:00:00Z",
                        "20"
                )
        );
        transactionRepository.save(
                transaction(
                        "outgoing",
                        contextId,
                        accountId,
                        otherAccountId,
                        "2026-01-01T00:00:00Z",
                        "40"
                )
        );

        ClientApplication client = new ClientApplication(
                new Application(
                        new ApplicationConfiguration(
                                accountRepository,
                                contextRepository,
                                transactionRepository
                        )
                )
        );

        client.loadIntoFinancialContext(contextId);
        client.loadAccount(accountId);

        List<AccountTransactionSummary> result = client
                .listTransactionsSummaryForCurrentAccount();

        assertEquals(2, result.size());
        assertEquals(monetaryValue("60"), result.get(0).totalAfterTransaction());
        assertEquals(monetaryValue("80"), result.get(1).totalAfterTransaction());
    }

    private TransactionRecord transaction(
            String transactionId,
            FinancialContextId contextId,
            AccountRecordId origin,
            AccountRecordId target,
            String dateTime,
            String value
    ) {

        return new TransactionRecord(
                new TransactionRecordId(transactionId, contextId),
                origin,
                target,
                Instant.parse(dateTime),
                monetaryValue(value)
        );
    }

    private MonetaryValue monetaryValue(String value) {
        return new MonetaryValue(new BigDecimal(value));
    }
}
