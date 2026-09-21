package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * SQLite implementation of {@link AccountRepository}.
 *
 * <p>This class translates account persistence operations into SQLite
 * operations.</p>
 */
public class SQLiteAccountRepository implements AccountRepository {

    private static final String INSERT = """
            INSERT INTO accounts (
                financial_context_id, account_id, name, initial_amount,
                parent_financial_context_id, parent_account_id,
                overridden_attributes
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(financial_context_id, account_id) DO UPDATE SET
                name = excluded.name,
                initial_amount = excluded.initial_amount,
                parent_financial_context_id = excluded.parent_financial_context_id,
                parent_account_id = excluded.parent_account_id,
                overridden_attributes = excluded.overridden_attributes
            """;

    private static final String SELECT_BY_CONTEXT = """
            SELECT financial_context_id, account_id, name, initial_amount,
                   parent_financial_context_id, parent_account_id,
                   overridden_attributes
            FROM accounts
            WHERE financial_context_id = ?
            """;

    private static final String SELECT_BY_ID = """
            SELECT financial_context_id, account_id, name, initial_amount,
                   parent_financial_context_id, parent_account_id,
                   overridden_attributes
            FROM accounts
            WHERE financial_context_id = ? AND account_id = ?
            """;

    private final JdbcRepository<AccountRecord> jdbcRepository;

    public SQLiteAccountRepository(DataSource dataSource) {
        this.jdbcRepository = new JdbcRepository<>(
                dataSource,
                SQLiteAccountRepository::mapAccount
        );
    }

    @Override
    public AccountRecord save(AccountRecord account) {
        AccountRecordId id = account.accountRecordId();

        return jdbcRepository.save(
                INSERT,
                account,
                id.financialContextId().financialContextId(),
                id.accountRecordId(),
                account.name(),
                account.initialAmount().toString(),
                parentFinancialContextId(account),
                parentAccountId(account),
                account.overriddenAttributes()
        );
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(
            FinancialContextId financialContextId) {

        return jdbcRepository.find(
                SELECT_BY_CONTEXT,
                financialContextId.financialContextId()
        );

    }

    @Override
    public Optional<AccountRecord> findById(AccountRecordId id) {

        return jdbcRepository.findSingle(
                SELECT_BY_ID,
                id.financialContextId().financialContextId(),
                id.accountRecordId()
        );

    }

    private static AccountRecord mapAccount(ResultSet resultSet)
            throws SQLException {
        FinancialContextId financialContextId = new FinancialContextId(
                resultSet.getString("financial_context_id")
        );

        return new AccountRecord(
                new AccountRecordId(
                        resultSet.getString("account_id"),
                        financialContextId
                ),
                resultSet.getString("name"),
                MonetaryValue.parse(resultSet.getString("initial_amount")),
                parentAccountRecordId(resultSet),
                resultSet.getInt("overridden_attributes")
        );
    }

    private static String parentFinancialContextId(AccountRecord account) {
        return account.parentAccountRecordId() == null
                ? null
                : account.parentAccountRecordId().financialContextId()
                .financialContextId();
    }

    private static String parentAccountId(AccountRecord account) {
        return account.parentAccountRecordId() == null
                ? null
                : account.parentAccountRecordId().accountRecordId();
    }

    private static AccountRecordId parentAccountRecordId(
            ResultSet resultSet
    ) throws SQLException {
        String parentAccountId = resultSet.getString("parent_account_id");
        String parentFinancialContextId = resultSet.getString(
                "parent_financial_context_id"
        );

        if (parentAccountId == null || parentFinancialContextId == null) {
            return null;
        }

        return new AccountRecordId(
                parentAccountId,
                new FinancialContextId(parentFinancialContextId)
        );
    }

}
