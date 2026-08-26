package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.client.data.account.AccountTransactionSummary;
import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientAccountTest {

    @Test
    void computesAccountHistoriesAndTotalsUpToTheSelectedDate() {

        FinancialContextId context = new FinancialContextId("context");
        AccountRecordId accountId = new AccountRecordId("account", context);
        AccountRecordId otherAccountId = new AccountRecordId("other", context);
        AccountRecord account = new AccountRecord(
                accountId,
                "Checking",
                monetaryValue("100")
        );
        AccountRecord otherAccount = new AccountRecord(
                otherAccountId,
                "Savings",
                monetaryValue("0")
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
        TransactionRecord futureTransaction = transaction(
                "3",
                context,
                accountId,
                otherAccountId,
                "2026-01-03T00:00:00Z",
                "10"
        );

        FinancialContextView view = new FinancialContextView(
                new FinancialContextRecord(context, "Personal"),
                LocalDate.parse("2026-01-02"),
                List.of(account, otherAccount),
                List.of(incomingSecond, futureTransaction, outgoingFirst)
        );

        ClientAccount computedAccount = view.findAccount(accountId)
                .orElseThrow();
        ClientAccount computedOtherAccount = view.findAccount(otherAccountId)
                .orElseThrow();

        List<AccountTransactionSummary> result = computedAccount
                .transactionSummaries();

        assertEquals(2, result.size());
        assertEquals(outgoingFirst, result.get(0).transaction());
        assertEquals(monetaryValue("60"), result.get(0).totalAfterTransaction());
        assertEquals(incomingSecond, result.get(1).transaction());
        assertEquals(monetaryValue("80"), result.get(1).totalAfterTransaction());
        assertEquals(monetaryValue("80"), computedAccount.currentTotal());
        assertEquals(monetaryValue("20"), computedOtherAccount.currentTotal());
    }

    @Test
    void usesTheInitialAmountWhenAnAccountHasNoTransactions() {
        FinancialContextId context = new FinancialContextId("context");
        AccountRecordId accountId = new AccountRecordId("account", context);
        AccountRecord account = new AccountRecord(
                accountId,
                "Checking",
                monetaryValue("100")
        );

        FinancialContextView view = new FinancialContextView(
                new FinancialContextRecord(context, "Personal"),
                LocalDate.parse("2026-01-02"),
                List.of(account),
                List.of()
        );

        ClientAccount computedAccount = view.findAccount(accountId)
                .orElseThrow();

        assertEquals(List.of(), computedAccount.transactionSummaries());
        assertEquals(monetaryValue("100"), computedAccount.currentTotal());
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
