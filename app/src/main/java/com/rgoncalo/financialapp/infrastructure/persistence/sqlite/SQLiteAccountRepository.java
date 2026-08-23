package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Collection;
import java.util.Optional;

/**
 * SQLite implementation of {@link AccountRepository}.
 *
 * <p>This class translates account persistence operations into SQLite
 * operations.</p>
 */
public class SQLiteAccountRepository implements AccountRepository {

    private final SQLiteRepository<AccountRecord> sqliteRepository;

    public SQLiteAccountRepository(SQLiteRepository<AccountRecord> sqliteRepository) {
        this.sqliteRepository = sqliteRepository;
    }

    @Override
    public AccountRecord save(AccountRecord account) {

        return sqliteRepository.save(account);

    }

    @Override
    public Collection<AccountRecord> listAccountsSummary(
            FinancialContextId financialContextId) {

        return sqliteRepository.findByRecordValues(new AccountRecord(new AccountRecordId(null, financialContextId), null, null));

    }

    @Override
    public Optional<AccountRecord> findById(AccountRecordId id) {

        return sqliteRepository.findSingleByRecordValue(
                new AccountRecord(id, null, null)
        );

    }

}