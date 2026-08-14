package com.rgoncalo.financialapp.infrastructure.database.sqlite;

import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteAccountRepositoryTest {

    private Connection connection;
    private SQLiteAccountRepository repository;

    @BeforeEach
    void setUp() throws SQLException {

        connection = DriverManager.getConnection(
                "jdbc:sqlite::memory:"
        );

        SQLiteSchema.initialize(connection);

        repository =
                new SQLiteAccountRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void savePersistsAccount() throws SQLException {

        Account account =
                new Account(
                        "account-1",
                        "Checking",
                        new MonetaryValue(
                                new BigDecimal("125.50")
                        )
                );

        repository.save(account);

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

            statement.setString(1, "account-1");

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                assertTrue(resultSet.next());

                assertEquals(
                        "account-1",
                        resultSet.getString("id")
                );

                assertEquals(
                        "Checking",
                        resultSet.getString("name")
                );

                assertEquals(
                        0,
                        new BigDecimal("125.50")
                                .compareTo(
                                        resultSet.getBigDecimal(
                                                "initial_amount_value"
                                        )
                                )
                );

                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    void findByIdReturnsStoredAccount() {

        repository.save(
                new Account(
                        "account-1",
                        "Checking",
                        new MonetaryValue(
                                new BigDecimal("125.50")
                        )
                )
        );

        Optional<Account> result =
                repository.findById("account-1");

        assertTrue(result.isPresent());

        Account account = result.orElseThrow();

        assertEquals(
                "account-1",
                account.getId()
        );

        assertEquals(
                "Checking",
                account.getName()
        );

        assertEquals(
                0,
                new BigDecimal("125.50")
                        .compareTo(
                                account.getInitialAmount()
                                        .getValue()
                        )
        );
    }

    @Test
    void findByIdReturnsEmptyWhenAccountDoesNotExist() {

        Optional<Account> result =
                repository.findById(
                        "does-not-exist"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void listAccountsSummaryReturnsStoredAccounts() {

        repository.save(
                new Account(
                        "account-1",
                        "Checking",
                        new MonetaryValue(
                                new BigDecimal("100.00")
                        )
                )
        );

        repository.save(
                new Account(
                        "account-2",
                        "Savings",
                        new MonetaryValue(
                                new BigDecimal("500.00")
                        )
                )
        );

        Collection<Account> result =
                repository.listAccountsSummary();

        assertEquals(
                Set.of(
                        new Account(
                                "account-1",
                                "Checking",
                                null
                        ),
                        new Account(
                                "account-2",
                                "Savings",
                                null
                        )
                ),
                Set.copyOf(result)
        );
    }
}