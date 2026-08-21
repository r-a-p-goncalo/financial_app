package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.Optional;

public interface TransactionRepository {

    /**
     * Persists the supplied transaction.
     *
     * @param transaction transaction to persist
     * @return the persisted transaction
     */
    TransactionRecord save(TransactionRecord transaction);

    /**
     * Returns the transactions belonging to a financial context.
     *
     * @param financialContextId the financial context
     * @return the transactions in that context
     */
    Collection<TransactionRecord> listTransactionsSummary(
            FinancialContextId financialContextId
    );

    /**
     * Queries a transaction by its identity.
     *
     * @param id transaction identity
     * @return the transaction, if present
     */
    Optional<TransactionRecord> findById(TransactionRecordId id);
}