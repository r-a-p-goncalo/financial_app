package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;

/**
 * Lists the transactions that change the balance of one account.
 */
public class ListTransactionsSummaryForAccount {

    private final TransactionRepository transactionRepository;

    public ListTransactionsSummaryForAccount(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public Collection<TransactionRecord> execute(
            ListTransactionsSummaryForAccountRequest request
    ) {

        return transactionRepository.listTransactionsSummaryForAccount(
                request.accountRecordId()
        );
    }
}
