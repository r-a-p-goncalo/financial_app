package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.Optional;

public class SQLiteTransactionRepository
        implements TransactionRepository {

    private final SQLiteRepository<TransactionRecord> sqliteRepository;

    public SQLiteTransactionRepository(SQLiteRepository<TransactionRecord> sqliteRepository) {
        this.sqliteRepository = sqliteRepository;
    }

    @Override
    public TransactionRecord save(
            TransactionRecord transaction
    ) {

        return sqliteRepository.save(
                transaction
        );
    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummary(
            FinancialContextId financialContextId
    ) {

        return sqliteRepository.findByRecordValues(new TransactionRecord(new TransactionRecordId(null, financialContextId), null, null, null, null));

    }

    @Override
    public Optional<TransactionRecord> findById(
            TransactionRecordId id
    ) {

        return sqliteRepository.findSingleByRecordValue(
                new TransactionRecord(id, null, null, null, null)
        );
    }
}