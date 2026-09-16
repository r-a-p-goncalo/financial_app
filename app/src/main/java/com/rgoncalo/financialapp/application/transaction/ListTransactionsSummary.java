package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;

public class ListTransactionsSummary {

    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public ListTransactionsSummary(
            TransactionRepository transactionRepository,
            FinancialContextAuthorization authorization
    ) {
        this.transactionRepository = transactionRepository;
        this.authorization = authorization;
    }

    public Collection<TransactionRecord> execute(
            ListTransactionsSummaryRequest request
    ) {
        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.READ
        );
        return transactionRepository.listTransactionsSummary(
                request.financialContextId()
        );
    }
}
