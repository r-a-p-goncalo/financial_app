package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;

import java.util.Optional;

/**
 * Retrieves one account by identity.
 */
public class GetAccount {

    private final AccountRepository accountRepository;

    public GetAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<AccountRecord> execute(
            GetAccountRequest request
    ) {

        return accountRepository.findById(
                request.accountRecordId()
        );
    }
}
