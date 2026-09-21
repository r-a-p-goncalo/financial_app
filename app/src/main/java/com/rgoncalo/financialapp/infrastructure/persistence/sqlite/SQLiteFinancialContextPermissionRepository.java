package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class SQLiteFinancialContextPermissionRepository
        implements FinancialContextPermissionRepository {

    private static final String INSERT = """
            INSERT INTO financial_context_permissions (
                financial_context_id, user_id, permission, granted_by_user_id,
                granted_at
            ) VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(financial_context_id, user_id) DO UPDATE SET
                permission = excluded.permission,
                granted_by_user_id = excluded.granted_by_user_id,
                granted_at = excluded.granted_at
            """;

    private static final String SELECT_BY_USER_AND_CONTEXT = """
            SELECT financial_context_id, user_id, permission,
                   granted_by_user_id, granted_at
            FROM financial_context_permissions
            WHERE user_id = ? AND financial_context_id = ?
            """;

    private static final String SELECT_BY_USER = """
            SELECT financial_context_id, user_id, permission,
                   granted_by_user_id, granted_at
            FROM financial_context_permissions
            WHERE user_id = ?
            """;

    private static final String SELECT_BY_CONTEXT = """
            SELECT financial_context_id, user_id, permission,
                   granted_by_user_id, granted_at
            FROM financial_context_permissions
            WHERE financial_context_id = ?
            """;

    private final JdbcRepository<FinancialContextPermissionRecord>
            jdbcRepository;

    public SQLiteFinancialContextPermissionRepository(DataSource dataSource) {
        this.jdbcRepository = new JdbcRepository<>(
                dataSource,
                SQLiteFinancialContextPermissionRepository::mapPermission
        );
    }

    @Override
    public FinancialContextPermissionRecord save(
            FinancialContextPermissionRecord permission
    ) {
        return jdbcRepository.save(
                INSERT,
                permission,
                permission.financialContextId().financialContextId(),
                permission.userId().userId(),
                permission.permission().name(),
                permission.grantedByUserId().userId(),
                permission.grantedAt().toString()
        );
    }

    @Override
    public Optional<FinancialContextPermissionRecord> findByUserAndContext(
            UserId userId,
            FinancialContextId financialContextId
    ) {
        return jdbcRepository.findSingle(
                SELECT_BY_USER_AND_CONTEXT,
                userId.userId(),
                financialContextId.financialContextId()
        );
    }

    @Override
    public Collection<FinancialContextPermissionRecord> listByUserId(
            UserId userId
    ) {
        return jdbcRepository.find(SELECT_BY_USER, userId.userId());
    }

    @Override
    public Collection<FinancialContextPermissionRecord>
    listByFinancialContextId(FinancialContextId financialContextId) {
        return jdbcRepository.find(
                SELECT_BY_CONTEXT,
                financialContextId.financialContextId()
        );
    }

    private static FinancialContextPermissionRecord mapPermission(
            ResultSet resultSet
    ) throws SQLException {
        return new FinancialContextPermissionRecord(
                new FinancialContextId(
                        resultSet.getString("financial_context_id")
                ),
                new UserId(resultSet.getString("user_id")),
                FinancialContextPermission.valueOf(
                        resultSet.getString("permission")
                ),
                new UserId(resultSet.getString("granted_by_user_id")),
                java.time.Instant.parse(resultSet.getString("granted_at"))
        );
    }
}
