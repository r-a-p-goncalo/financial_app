package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;

public class ListTransactionsSummary {

    private final TransactionRepository transactionRepository;

    public ListTransactionsSummary(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public Collection<TransactionRecord> execute(
            ListTransactionsSummaryRequest request
    ) {
        return transactionRepository.listTransactionsSummary(
                request.financialContextId()
        );
    }
}