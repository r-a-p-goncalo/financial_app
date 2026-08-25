package com.rgoncalo.financialapp.infrastructure.persistence.memory;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTransactionRepository
        implements TransactionRepository {

    private final Map<TransactionRecordId, TransactionRecord>
            transactions = new LinkedHashMap<>();

    @Override
    public TransactionRecord save(
            TransactionRecord transaction
    ) {

        transactions.put(
                transaction.transactionRecordId(),
                transaction
        );

        return transaction;
    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummary(
            FinancialContextId financialContextId
    ) {

        return transactions
                .values()
                .stream()
                .filter(transaction ->
                        transaction
                                .transactionRecordId()
                                .financialContextId()
                                .equals(financialContextId)
                )
                .toList();
    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummaryForAccount(
            AccountRecordId accountRecordId
    ) {

        return transactions
                .values()
                .stream()
                .filter(transaction ->
                        accountRecordId.equals(
                                transaction.originAccountId()
                        )
                                || accountRecordId.equals(
                                transaction.targetAccountId()
                        )
                )
                .toList();
    }

    @Override
    public Optional<TransactionRecord> findById(
            TransactionRecordId id
    ) {

        return Optional.ofNullable(
                transactions.get(id)
        );
    }

    public int size() {
        return transactions.size();
    }

    public Collection<TransactionRecord> transactions() {
        return List.copyOf(
                transactions.values()
        );
    }
}
