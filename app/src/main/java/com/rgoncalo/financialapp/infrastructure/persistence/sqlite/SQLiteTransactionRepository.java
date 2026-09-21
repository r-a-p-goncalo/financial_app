package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class SQLiteTransactionRepository
        implements TransactionRepository {

    private static final String INSERT = """
            INSERT INTO transactions (
                financial_context_id,
                transaction_id,
                origin_account_id,
                target_account_id,
                date_time,
                value,
                parent_financial_context_id,
                parent_transaction_id,
                overridden_attributes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(financial_context_id, transaction_id) DO UPDATE SET
                origin_account_id = excluded.origin_account_id,
                target_account_id = excluded.target_account_id,
                date_time = excluded.date_time,
                value = excluded.value,
                parent_financial_context_id = excluded.parent_financial_context_id,
                parent_transaction_id = excluded.parent_transaction_id,
                overridden_attributes = excluded.overridden_attributes
            """;

    private static final String SELECT_BY_CONTEXT = """
            SELECT financial_context_id, transaction_id, origin_account_id,
                   target_account_id, date_time, value,
                   parent_financial_context_id, parent_transaction_id,
                   overridden_attributes
            FROM transactions
            WHERE financial_context_id = ?
            """;

    private static final String SELECT_BY_ACCOUNT = """
            SELECT financial_context_id, transaction_id, origin_account_id,
                   target_account_id, date_time, value,
                   parent_financial_context_id, parent_transaction_id,
                   overridden_attributes
            FROM transactions
            WHERE financial_context_id = ?
              AND (origin_account_id = ? OR target_account_id = ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT financial_context_id, transaction_id, origin_account_id,
                   target_account_id, date_time, value,
                   parent_financial_context_id, parent_transaction_id,
                   overridden_attributes
            FROM transactions
            WHERE financial_context_id = ? AND transaction_id = ?
            """;

    private final JdbcRepository<TransactionRecord> jdbcRepository;

    public SQLiteTransactionRepository(DataSource dataSource) {
        this.jdbcRepository = new JdbcRepository<>(
                dataSource,
                SQLiteTransactionRepository::mapTransaction
        );
    }

    @Override
    public TransactionRecord save(
            TransactionRecord transaction
    ) {

        TransactionRecordId id = transaction.transactionRecordId();

        return jdbcRepository.save(
                INSERT,
                transaction,
                id.financialContextId().financialContextId(),
                id.transactionRecordId(),
                accountId(transaction.originAccountId()),
                accountId(transaction.targetAccountId()),
                transaction.dateTime().toString(),
                transaction.value().toString(),
                parentFinancialContextId(transaction),
                parentTransactionId(transaction),
                transaction.overriddenAttributes()
        );
    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummary(
            FinancialContextId financialContextId
    ) {

        return jdbcRepository.find(
                SELECT_BY_CONTEXT,
                financialContextId.financialContextId()
        );

    }

    @Override
    public Collection<TransactionRecord> listTransactionsSummaryForAccount(
            AccountRecordId accountRecordId
    ) {

        return jdbcRepository.find(
                SELECT_BY_ACCOUNT,
                accountRecordId.financialContextId().financialContextId(),
                accountRecordId.accountRecordId(),
                accountRecordId.accountRecordId()
        );
    }

    @Override
    public Optional<TransactionRecord> findById(
            TransactionRecordId id
    ) {

        return jdbcRepository.findSingle(
                SELECT_BY_ID,
                id.financialContextId().financialContextId(),
                id.transactionRecordId()
        );
    }

    private static String accountId(AccountRecordId accountRecordId) {
        return accountRecordId == null ? null : accountRecordId.accountRecordId();
    }

    private static TransactionRecord mapTransaction(ResultSet resultSet)
            throws SQLException {
        FinancialContextId financialContextId = new FinancialContextId(
                resultSet.getString("financial_context_id")
        );

        return new TransactionRecord(
                new TransactionRecordId(
                        resultSet.getString("transaction_id"),
                        financialContextId
                ),
                accountRecordId(
                        resultSet.getString("origin_account_id"),
                        financialContextId
                ),
                accountRecordId(
                        resultSet.getString("target_account_id"),
                        financialContextId
                ),
                java.time.Instant.parse(resultSet.getString("date_time")),
                MonetaryValue.parse(resultSet.getString("value")),
                parentTransactionRecordId(resultSet),
                resultSet.getInt("overridden_attributes")
        );
    }

    private static AccountRecordId accountRecordId(
            String accountId,
            FinancialContextId financialContextId
    ) {
        return accountId == null
                ? null
                : new AccountRecordId(accountId, financialContextId);
    }

    private static String parentFinancialContextId(
            TransactionRecord transaction
    ) {
        return transaction.parentTransactionRecordId() == null
                ? null
                : transaction.parentTransactionRecordId().financialContextId()
                .financialContextId();
    }

    private static String parentTransactionId(TransactionRecord transaction) {
        return transaction.parentTransactionRecordId() == null
                ? null
                : transaction.parentTransactionRecordId().transactionRecordId();
    }

    private static TransactionRecordId parentTransactionRecordId(
            ResultSet resultSet
    ) throws SQLException {
        String parentTransactionId = resultSet.getString(
                "parent_transaction_id"
        );
        String parentFinancialContextId = resultSet.getString(
                "parent_financial_context_id"
        );

        if (parentTransactionId == null || parentFinancialContextId == null) {
            return null;
        }

        return new TransactionRecordId(
                parentTransactionId,
                new FinancialContextId(parentFinancialContextId)
        );
    }
}
