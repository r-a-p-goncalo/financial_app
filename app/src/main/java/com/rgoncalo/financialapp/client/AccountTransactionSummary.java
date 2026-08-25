package com.rgoncalo.financialapp.client;

import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Objects;

/**
 * Client-side view of a transaction and the account balance it leaves behind.
 */
public record AccountTransactionSummary(
        TransactionRecord transaction,
        MonetaryValue totalAfterTransaction
) {

    public AccountTransactionSummary {
        Objects.requireNonNull(transaction);
        Objects.requireNonNull(totalAfterTransaction);
    }
}
