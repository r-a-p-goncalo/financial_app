package com.rgoncalo.financialapp.infrastructure.database.sqlite;

import com.rgoncalo.financialapp.application.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * SQLite implementation of {@link AccountRepository}.
 *
 * <p>This class translates account persistence operations into SQLite
 * operations.</p>
 */
public class SQLiteAccountRepository implements AccountRepository {

    private final Connection connection;

    public SQLiteAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public AccountRecord save(AccountRecord account) {

        String sql = """
                INSERT INTO %s (
                    %s,
                    %s,
                    %s,
                    %s
                )
                
                VALUES (?, ?, ?, ?)
                """.formatted(
                SQLiteSchema.ACCOUNT_TABLE_NAME,
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME,
                SQLiteSchema.INITIAL_AMOUNT_VALUE_COLUMN_NAME,
                SQLiteSchema.FINANCIAL_CONTEXT_ID_COLUMN_NAME
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, account.id());
            statement.setString(2, account.name());
            statement.setBigDecimal(
                    3,
                    account.initial_value().getValue()
            );
            statement.setString(4, account.financialContextId());

            statement.executeUpdate();

            return account;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not save account: " + account.id(),
                    exception
            );
        }
    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(
            String financialContextId) {

        String sql = """
                SELECT %s, %s
                FROM %s
                WHERE %s = ?
                """.formatted(
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME,
                SQLiteSchema.ACCOUNT_TABLE_NAME,
                SQLiteSchema.FINANCIAL_CONTEXT_ID_COLUMN_NAME
        );

        Collection<AccountRecord> accounts = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, financialContextId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String id = resultSet.getString(
                            SQLiteSchema.ID_COLUMN_NAME
                    );

                    String name = resultSet.getString(
                            SQLiteSchema.NAME_COLUMN_NAME
                    );

                    accounts.add(
                            new AccountRecord(
                                    id,
                                    name,
                                    null,
                                    financialContextId
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not list accounts for financial context: "
                            + financialContextId,
                    exception
            );
        }

        return accounts;
    }

    @Override
    public Optional<AccountRecord> findById(String id) {

        String sql = """
                SELECT
                    %s,
                    %s,
                    %s,
                    %s
                FROM %s
                WHERE %s = ?
                """.formatted(
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME,
                SQLiteSchema.INITIAL_AMOUNT_VALUE_COLUMN_NAME,
                SQLiteSchema.FINANCIAL_CONTEXT_ID_COLUMN_NAME,
                SQLiteSchema.ACCOUNT_TABLE_NAME,
                SQLiteSchema.ID_COLUMN_NAME
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String accountId = resultSet.getString(
                        SQLiteSchema.ID_COLUMN_NAME
                );

                String name = resultSet.getString(
                        SQLiteSchema.NAME_COLUMN_NAME
                );

                BigDecimal value = resultSet.getBigDecimal(
                        SQLiteSchema.INITIAL_AMOUNT_VALUE_COLUMN_NAME
                );

                MonetaryValue initialAmount =
                        new MonetaryValue(value);

                String financialContextId = resultSet.getString(
                        SQLiteSchema.FINANCIAL_CONTEXT_ID_COLUMN_NAME
                );

                AccountRecord account =
                        new AccountRecord(
                                accountId,
                                name,
                                initialAmount,
                                financialContextId
                        );

                return Optional.of(account);
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not find account: " + id,
                    exception
            );
        }
    }
}