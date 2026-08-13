package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.account.Account;

import java.util.Collection;
import java.util.Optional;

/**
 * Provides persistence operations for {@link Account} objects.
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
    Account save(Account account);

    /**
     *
     * @return the partial information for accounts available
     */
    Collection<AccountRecord> listAccountsSummary();

    /**
     *
     * Queries a specific account
     *
     * @param id, the id of the account
     * @return the account with the id, possibly null
     */
    Optional<Account> findById(String id);
}

