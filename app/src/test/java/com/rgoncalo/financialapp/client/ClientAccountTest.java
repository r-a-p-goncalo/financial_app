package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientAccountTest {

    @Test
    void createsChronologicalRunningTotalsForIncomingAndOutgoingTransactions() {

        FinancialContextId context = new FinancialContextId("context");
        AccountRecordId accountId = new AccountRecordId("account", context);
        AccountRecordId otherAccountId = new AccountRecordId("other", context);
        ClientAccount account = new ClientAccount(
                new AccountRecord(
                        accountId,
                        "Checking",
                        monetaryValue("100")
                )
        );

        TransactionRecord incomingSecond = transaction(
                "2",
                context,
                otherAccountId,
                accountId,
                "2026-01-02T00:00:00Z",
                "20"
        );
        TransactionRecord outgoingFirst = transaction(
                "1",
                context,
                accountId,
                otherAccountId,
                "2026-01-01T00:00:00Z",
                "40"
        );

        List<AccountTransactionSummary> result = account
                .summarizeTransactions(
                        List.of(incomingSecond, outgoingFirst)
                );

        assertEquals(outgoingFirst, result.get(0).transaction());
        assertEquals(monetaryValue("60"), result.get(0).totalAfterTransaction());
        assertEquals(incomingSecond, result.get(1).transaction());
        assertEquals(monetaryValue("80"), result.get(1).totalAfterTransaction());
    }

    private TransactionRecord transaction(
            String transactionId,
            FinancialContextId context,
            AccountRecordId origin,
            AccountRecordId target,
            String dateTime,
            String value
    ) {

        return new TransactionRecord(
                new TransactionRecordId(transactionId, context),
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
