package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;

/**
 * Lists the transactions that change the balance of one account.
 */
public class ListTransactionsSummaryForAccount {

    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public ListTransactionsSummaryForAccount(
            TransactionRepository transactionRepository,
            FinancialContextAuthorization authorization
    ) {
        this.transactionRepository = transactionRepository;
        this.authorization = authorization;
    }

    public Collection<TransactionRecord> execute(
            ListTransactionsSummaryForAccountRequest request
    ) {
        authorization.requirePermission(
                request.userId(),
                request.accountRecordId().financialContextId(),
                FinancialContextPermission.READ
        );

        return transactionRepository.listTransactionsSummaryForAccount(
                request.accountRecordId()
        );
    }
}
