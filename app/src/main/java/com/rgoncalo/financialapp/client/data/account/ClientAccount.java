package com.rgoncalo.financialapp.client.data.account;

import com.rgoncalo.financialapp.client.AccountTransactionSummary;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Client-side account data that adds balance calculations to the common
 * account and transaction records.
 */
public final class ClientAccount {

    private final AccountRecord account;

    public ClientAccount(AccountRecord account) {
        this.account = Objects.requireNonNull(account);
        Objects.requireNonNull(account.initialAmount());
    }

    public AccountRecord record() {
        return account;
    }

    /**
     * Produces a chronological balance history for transactions affecting this
     * account. Outgoing transactions decrease the total; incoming transactions
     * increase it.
     */
    public List<AccountTransactionSummary> summarizeTransactions(
            Collection<TransactionRecord> transactions
    ) {

        List<TransactionRecord> transactionsByTime = transactions
                .stream()
                .sorted(
                        Comparator.comparing(
                                        TransactionRecord::dateTime,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                                .thenComparing(
                                        transaction -> transaction
                                                .transactionRecordId()
                                                .transactionRecordId(),
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                )
                .toList();

        BigDecimal currentTotal = account.initialAmount().getValue();
        List<AccountTransactionSummary> summaries = new ArrayList<>();

        for (TransactionRecord transaction : transactionsByTime) {
            BigDecimal transactionValue = transaction.value().getValue();

            if (account.accountRecordId().equals(
                    transaction.originAccountId()
            )) {
                currentTotal = currentTotal.subtract(transactionValue);
            } else if (account.accountRecordId().equals(
                    transaction.targetAccountId()
            )) {
                currentTotal = currentTotal.add(transactionValue);
            } else {
                throw new IllegalArgumentException(
                        "Transaction does not affect account: "
                                + account.accountRecordId()
                );
            }

            summaries.add(
                    new AccountTransactionSummary(
                            transaction,
                            new MonetaryValue(currentTotal)
                    )
            );
        }

        return List.copyOf(summaries);
    }
}