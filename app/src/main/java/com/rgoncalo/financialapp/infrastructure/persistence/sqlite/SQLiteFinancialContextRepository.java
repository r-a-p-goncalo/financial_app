package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;

import java.sql.Connection;
import java.util.Collection;
import java.util.Optional;

/**
 * SQLite implementation of {@link FinancialContextRepository}.
 *
 * <p>This class translates financial context persistence operations
 * into SQLite operations.</p>
 */
public class SQLiteFinancialContextRepository
        implements FinancialContextRepository {

    private final SQLiteRepository<FinancialContextRecord> sqLiteRepository;

    public SQLiteFinancialContextRepository(SQLiteRepository<FinancialContextRecord> sqliteRepository) {
        this.sqLiteRepository = sqliteRepository;
    }

    @Override
    public FinancialContextRecord save(
            FinancialContextRecord financialContext) {

        return sqLiteRepository.save(financialContext);
    }

    @Override
    public Collection<FinancialContextRecord>
    listFinancialContextsSummary() {

        return sqLiteRepository.getAllRecords();
    }

    @Override
    public Optional<FinancialContextRecord> findById(
            FinancialContextId id) {

        return sqLiteRepository.findSingleByRecordValue(
                new FinancialContextRecord(id, null)
        );

}
}