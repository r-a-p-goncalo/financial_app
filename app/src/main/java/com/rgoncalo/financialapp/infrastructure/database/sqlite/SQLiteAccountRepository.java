package com.rgoncalo.financialapp.infrastructure.database.sqlite;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of {@link AccountRepository}.
 *
 * <p>This class translates account persistence operations into SQLite
 * operations</p>
 */
public class SQLiteAccountRepository implements AccountRepository {

    private final Connection connection;

    public SQLiteAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Account save(Account account) {

        String sql = """
                INSERT INTO account (
                    id,
                    name,
                    initial_amount_value
                )
                
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, account.getId());
            statement.setString(2, account.getName());
            statement.setBigDecimal(
                    3,
                    account.getInitialAmount().getValue()
            );

            statement.executeUpdate();

            return account;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not save account: " + account.getId(),
                    exception
            );
        }
    }

    @Override
    public Collection<Account> listAccountsSummary() {

        String sql =
                """
                SELECT id, name FROM account
                """;

        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");

                accounts.add(new Account(id, name, null));
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not list accounts",
                    exception
            );
        }

        return accounts;
    }

    @Override
    public Optional<Account> findById(String id) {

        String sql = """
                SELECT
                    id,
                    name,
                    initial_amount_value
                FROM account
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String accountId =
                        resultSet.getString("id");

                String name =
                        resultSet.getString("name");

                BigDecimal value =
                        resultSet.getBigDecimal(
                                "initial_amount_value"
                        );

                MonetaryValue initialAmount =
                        new MonetaryValue(
                                value
                        );

                Account account =
                        new Account(
                                accountId,
                                name,
                                initialAmount
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