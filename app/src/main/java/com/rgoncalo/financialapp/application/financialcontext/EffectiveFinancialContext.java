package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;
import java.util.List;

/**
 * A context after all inherited attributes have been resolved.
 */
public record EffectiveFinancialContext(
        FinancialContextRecord financialContext,
        List<AccountRecord> accounts,
        List<TransactionRecord> transactions
) {

    public EffectiveFinancialContext(
            FinancialContextRecord financialContext,
            Collection<AccountRecord> accounts,
            Collection<TransactionRecord> transactions
    ) {
        this(financialContext, List.copyOf(accounts), List.copyOf(transactions));
    }
}
