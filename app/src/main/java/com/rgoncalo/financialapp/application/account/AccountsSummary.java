package com.rgoncalo.financialapp.application.account;

import java.util.Collection;

/**
 * Application use case for listing accounts.
 *
 * <p></p>
 */
public class AccountsSummary {

    private final AccountRepository accountRepository;

    public AccountsSummary(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Returns a list of accounts
     *
     * @param request, defines which accounts to return
     * @return a collection of accounts
     */
    public Collection<AccountRecord> execute(AccountsSummaryRequest request) {

        return accountRepository.listAccountsSummary(request.financialContextId());
    }

}
