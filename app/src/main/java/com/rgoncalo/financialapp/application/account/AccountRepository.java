package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

import java.util.Collection;
import java.util.Optional;

// TODO: if this gets too complex, separate the logic of queries and saving data

/**
 * Provides persistence operations for {@link AccountRecord} objects.
 *
 * <p>The repository abstracts the storage mechanism from the application
 * layer. Implementations are responsible for storing and retrieving
 * accounts, while callers depend only on the repository contract.</p>
 */
public interface AccountRepository {

    /**
     * Persists the supplied account.
     *
     * @param account, account to persist
     */
    AccountRecord save(AccountRecord account);

    /**
     *
     * @return the partial information for accounts available
     */
    Collection<AccountRecord> listAccountsSummary(FinancialContextId financialContextId);

    /**
     *
     * Queries a specific account
     *
     * @param id, the id of the account
     * @return the account with the id, possibly null
     */
    Optional<AccountRecord> findById(AccountRecordId id);

}
