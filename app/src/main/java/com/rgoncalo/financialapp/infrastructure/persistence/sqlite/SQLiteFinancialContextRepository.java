package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private final Connection connection;

    public SQLiteFinancialContextRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public FinancialContextRecord save(
            FinancialContextRecord financialContext) {

        String sql = """
                INSERT INTO %s (
                    %s,
                    %s
                )
                
                VALUES (?, ?)
                """.formatted(
                SQLiteSchema.FINANCIAL_CONTEXT_TABLE_NAME,
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    financialContext.id()
            );

            statement.setString(
                    2,
                    financialContext.name()
            );

            statement.executeUpdate();

            return financialContext;

        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not save financial context: "
                            + financialContext.id(),
                    exception
            );
        }
    }

    @Override
    public Collection<FinancialContextRecord>
    listFinancialContextsSummary() {

        String sql = """
                SELECT
                    %s,
                    %s
                FROM %s
                """.formatted(
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME,
                SQLiteSchema.FINANCIAL_CONTEXT_TABLE_NAME
        );

        Collection<FinancialContextRecord> financialContexts =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                String id =
                        resultSet.getString(
                                SQLiteSchema.ID_COLUMN_NAME
                        );

                String name =
                        resultSet.getString(
                                SQLiteSchema.NAME_COLUMN_NAME
                        );

                financialContexts.add(
                        new FinancialContextRecord(
                                id,
                                name
                        )
                );
            }

        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not list financial contexts",
                    exception
            );
        }

        return financialContexts;
    }

    @Override
    public Optional<FinancialContextRecord> findById(
            String id) {

        String sql = """
                SELECT
                    %s,
                    %s
                FROM %s
                WHERE %s = ?
                """.formatted(
                SQLiteSchema.ID_COLUMN_NAME,
                SQLiteSchema.NAME_COLUMN_NAME,
                SQLiteSchema.FINANCIAL_CONTEXT_TABLE_NAME,
                SQLiteSchema.ID_COLUMN_NAME
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String financialContextId =
                        resultSet.getString(
                                SQLiteSchema.ID_COLUMN_NAME
                        );

                String name =
                        resultSet.getString(
                                SQLiteSchema.NAME_COLUMN_NAME
                        );

                return Optional.of(
                        new FinancialContextRecord(
                                financialContextId,
                                name
                        )
                );
            }

        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not find financial context: " + id,
                    exception
            );
        }
    }
}