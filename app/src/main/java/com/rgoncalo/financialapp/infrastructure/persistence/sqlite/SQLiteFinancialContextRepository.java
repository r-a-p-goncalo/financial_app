package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.SQLiteRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.SQLiteTypeConverters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteSchema.FINANCIAL_CONTEXT_TABLE_NAME;

/**
 * SQLite implementation of {@link FinancialContextRepository}.
 *
 * <p>This class translates financial context persistence operations
 * into SQLite operations.</p>
 */
public class SQLiteFinancialContextRepository
        implements FinancialContextRepository {

    private final SQLiteRepository<FinancialContextRecord> sqLiteRepository;

    public SQLiteFinancialContextRepository(Connection connection) {
        this.sqLiteRepository = new SQLiteRepository<FinancialContextRecord>(
                connection,
                FinancialContextRecord.class,
                FINANCIAL_CONTEXT_TABLE_NAME,
                new SQLiteTypeConverters()
        );
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

        return sqLiteRepository.findSingleByRecordValue(id);

}
}