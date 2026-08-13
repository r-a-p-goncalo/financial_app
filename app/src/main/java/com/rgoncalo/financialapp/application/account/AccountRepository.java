package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.account.Account;

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

    Optional<Account> findById(String id);
}

