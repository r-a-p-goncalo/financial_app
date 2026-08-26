package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static final String INSERT = """
            INSERT INTO financial_contexts (financial_context_id, name)
            VALUES (?, ?)
            """;

    private static final String SELECT_ALL = """
            SELECT financial_context_id, name
            FROM financial_contexts
            """;

    private static final String SELECT_BY_ID = """
            SELECT financial_context_id, name
            FROM financial_contexts
            WHERE financial_context_id = ?
            """;

    private final SQLiteRepository<FinancialContextRecord> sqliteRepository;

    public SQLiteFinancialContextRepository(Connection connection) {
        this.sqliteRepository = new SQLiteRepository<>(
                connection,
                SQLiteFinancialContextRepository::mapFinancialContext
        );
    }

    @Override
    public FinancialContextRecord save(
            FinancialContextRecord financialContext) {

        return sqliteRepository.save(
                INSERT,
                financialContext,
                financialContext.financialContextId().financialContextId(),
                financialContext.name()
        );
    }

    @Override
    public Collection<FinancialContextRecord>
    listFinancialContextsSummary() {

        return sqliteRepository.find(SELECT_ALL);
    }

    @Override
    public Optional<FinancialContextRecord> findById(
            FinancialContextId id) {

        return sqliteRepository.findSingle(
                SELECT_BY_ID,
                id.financialContextId()
        );

    }

    private static FinancialContextRecord mapFinancialContext(
            ResultSet resultSet
    ) throws SQLException {
        return new FinancialContextRecord(
                new FinancialContextId(
                        resultSet.getString("financial_context_id")
                ),
                resultSet.getString("name")
        );
    }
}
