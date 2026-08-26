package com.rgoncalo.financialapp.client.data.financialcontext;

import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Objects;

/**
 * A transaction in a financial-context view, together with the resulting
 * totals for its origin and target accounts.
 */
public record FinancialContextTransactionSummary(
        TransactionRecord transaction,
        AccountBalance originBalance,
        AccountBalance targetBalance
) {

    public FinancialContextTransactionSummary {
        Objects.requireNonNull(transaction);
    }

    /**
     * The state of one account immediately after a transaction. A missing
     * balance on the enclosing summary represents an external transaction
     * side or an account not included in the view.
     */
    public record AccountBalance(
            ClientAccount account,
            MonetaryValue totalAfterTransaction
    ) {

        public AccountBalance {
            Objects.requireNonNull(account);
            Objects.requireNonNull(totalAfterTransaction);
        }
    }
}
