package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.SQLiteRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.SQLiteTypeConverters;

import java.sql.Connection;
import java.util.Collection;
import java.util.Optional;

import static com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteSchema.TRANSACTION_TABLE_NAME;

public class SQLiteTransactionRepository
        implements TransactionRepository {

    private final SQLiteRepository<TransactionRecord>
            sqliteRepository;

    public SQLiteTransactionRepository(
            Connection connection
    ) {

        this.sqliteRepository =
                new SQLiteRepository<>(
                        connection,
                        TransactionRecord.class,
                        TRANSACTION_TABLE_NAME,
                        new SQLiteTypeConverters()
                );
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

        return sqliteRepository.findByRecordValues(
                financialContextId
        );
    }

    @Override
    public Optional<TransactionRecord> findById(
            TransactionRecordId id
    ) {

        return sqliteRepository.findSingleByRecordValue(
                id
        );
    }
}