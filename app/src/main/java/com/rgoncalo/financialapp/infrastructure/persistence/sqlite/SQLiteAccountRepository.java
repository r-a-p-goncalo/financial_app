package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.SQLiteQueryCondition;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.SQLiteRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.SQLiteTypeConverters;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteSchema.ACCOUNT_TABLE_NAME;

/**
 * SQLite implementation of {@link AccountRepository}.
 *
 * <p>This class translates account persistence operations into SQLite
 * operations.</p>
 */
public class SQLiteAccountRepository implements AccountRepository {

    private final SQLiteRepository<AccountRecord> sqliteRepository;

    public SQLiteAccountRepository(Connection connection) {
        this.sqliteRepository = new SQLiteRepository<AccountRecord>(connection, AccountRecord.class, ACCOUNT_TABLE_NAME, new SQLiteTypeConverters());
    }

    @Override
    public AccountRecord save(AccountRecord account) {

        return sqliteRepository.save(account);

    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(
            FinancialContextId financialContextId) {

        return sqliteRepository.findByRecordValues(financialContextId);

    }

    @Override
    public Optional<AccountRecord> findById(AccountRecordId id) {

        Collection<AccountRecord> accountRecordsWithId = sqliteRepository.findByRecordValues(id);

        if (accountRecordsWithId.size() > 1) {
            throw new PersistenceException("Multiple account records were gotten with ID");
        } else if (accountRecordsWithId.size() == 1) {
            return Optional.of(accountRecordsWithId.iterator().next());
        } else {
            return Optional.empty();
        }

    }

}