package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcRepository;

import javax.sql.DataSource;
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
            INSERT INTO financial_contexts (
                financial_context_id, name, parent_financial_context_id,
                overridden_attributes, is_live
            ) VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(financial_context_id) DO UPDATE SET
                name = excluded.name,
                parent_financial_context_id = excluded.parent_financial_context_id,
                overridden_attributes = excluded.overridden_attributes,
                is_live = excluded.is_live
            """;

    private static final String SELECT_ALL = """
            SELECT financial_context_id, name, parent_financial_context_id,
                   overridden_attributes, is_live
            FROM financial_contexts
            WHERE is_live = 1
            """;

    private static final String SELECT_CHILDREN = """
            SELECT financial_context_id, name, parent_financial_context_id,
                   overridden_attributes, is_live
            FROM financial_contexts
            WHERE parent_financial_context_id = ? AND is_live = 1
            """;

    private static final String SELECT_BY_ID = """
            SELECT financial_context_id, name, parent_financial_context_id,
                   overridden_attributes, is_live
            FROM financial_contexts
            WHERE financial_context_id = ? AND is_live = 1
            """;

    private static final String DELETE = """
            DELETE FROM financial_contexts
            WHERE financial_context_id = ?
            """;

    private final JdbcRepository<FinancialContextRecord> jdbcRepository;

    public SQLiteFinancialContextRepository(DataSource dataSource) {
        this.jdbcRepository = new JdbcRepository<>(
                dataSource,
                SQLiteFinancialContextRepository::mapFinancialContext
        );
    }

    @Override
    public FinancialContextRecord save(
            FinancialContextRecord financialContext) {

        return jdbcRepository.save(
                INSERT,
                financialContext,
                financialContext.financialContextId().financialContextId(),
                financialContext.name(),
                financialContext.parentFinancialContextId() == null ? null
                        : financialContext.parentFinancialContextId()
                        .financialContextId(),
                financialContext.overriddenAttributes()
                , financialContext.live() ? 1 : 0
        );
    }

    @Override
    public Collection<FinancialContextRecord>
    listFinancialContextsSummary() {

        return jdbcRepository.find(SELECT_ALL);
    }

    @Override
    public Collection<FinancialContextRecord> listChildren(
            FinancialContextId parentFinancialContextId
    ) {
        return jdbcRepository.find(
                SELECT_CHILDREN,
                parentFinancialContextId.financialContextId()
        );
    }

    @Override
    public Optional<FinancialContextRecord> findById(
            FinancialContextId id) {

        return jdbcRepository.findSingle(
                SELECT_BY_ID,
                id.financialContextId()
        );

    }

    @Override
    public void deletePermanently(FinancialContextId id) {
        jdbcRepository.executeUpdate(DELETE, id.financialContextId());
    }

    private static FinancialContextRecord mapFinancialContext(
            ResultSet resultSet
    ) throws SQLException {
        return new FinancialContextRecord(
                new FinancialContextId(
                        resultSet.getString("financial_context_id")
                ),
                resultSet.getString("name"),
                financialContextId(
                        resultSet.getString("parent_financial_context_id")
                ),
                resultSet.getInt("overridden_attributes"),
                resultSet.getInt("is_live") != 0
        );
    }

    private static FinancialContextId financialContextId(
            String financialContextId
    ) {
        return financialContextId == null
                ? null
                : new FinancialContextId(financialContextId);
    }
}
