package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.sql.Connection;
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
                financial_context_id, account_id, name, initial_amount
            ) VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_BY_CONTEXT = """
            SELECT financial_context_id, account_id, name, initial_amount
            FROM accounts
            WHERE financial_context_id = ?
            """;

    private static final String SELECT_BY_ID = """
            SELECT financial_context_id, account_id, name, initial_amount
            FROM accounts
            WHERE financial_context_id = ? AND account_id = ?
            """;

    private final SQLiteRepository<AccountRecord> sqliteRepository;

    public SQLiteAccountRepository(Connection connection) {
        this.sqliteRepository = new SQLiteRepository<>(
                connection,
                SQLiteAccountRepository::mapAccount
        );
    }

    @Override
    public AccountRecord save(AccountRecord account) {
        AccountRecordId id = account.accountRecordId();

        return sqliteRepository.save(
                INSERT,
                account,
                id.financialContextId().financialContextId(),
                id.accountRecordId(),
                account.name(),
                account.initialAmount().toString()
        );
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(
            FinancialContextId financialContextId) {

        return sqliteRepository.find(
                SELECT_BY_CONTEXT,
                financialContextId.financialContextId()
        );

    }

    @Override
    public Optional<AccountRecord> findById(AccountRecordId id) {

        return sqliteRepository.findSingle(
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
                MonetaryValue.parse(resultSet.getString("initial_amount"))
        );
    }

}
