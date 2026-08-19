package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;

import java.util.Collection;

/**
 * Application use case for listing accounts.
 *
 * <p></p>
 */
public class ListAccountsSummary {

    private final AccountRepository accountRepository;

    public ListAccountsSummary(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Returns a list of accounts
     *
     * @param request, defines which accounts to return
     * @return a collection of accounts
     */
    public Collection<AccountRecord> execute(ListAccountsSummaryRequest request) {

        return accountRepository.listAccountsSummary(request.financialContextId());
    }

}
