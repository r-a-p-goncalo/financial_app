package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.Optional;

public class RecordingTransactionRepository
        implements TransactionRepository {

    private final TransactionRepository delegate;

    private int saveCalls;

    public RecordingTransactionRepository(
            TransactionRepository delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    public TransactionRecord save(
            TransactionRecord transaction
    ) {

        saveCalls++;

        return delegate.save(
                transaction
        );
    }

    @Override
    public Collection<TransactionRecord>
    listTransactionsSummary(
            FinancialContextId financialContextId
    ) {

        return delegate.listTransactionsSummary(
                financialContextId
        );
    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummaryForAccount(
            AccountRecordId accountRecordId
    ) {

        return delegate.listTransactionsSummaryForAccount(
                accountRecordId
        );
    }

    @Override
    public Optional<TransactionRecord> findById(
            TransactionRecordId id
    ) {

        return delegate.findById(
                id
        );
    }

    public int saveCalls() {
        return saveCalls;
    }
}
