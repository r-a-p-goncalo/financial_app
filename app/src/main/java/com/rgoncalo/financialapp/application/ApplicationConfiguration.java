package com.rgoncalo.financialapp.application;

import com.rgoncalo.financialapp.application.account.AccountRepository;

public class ApplicationConfiguration {

    private final AccountRepository accountRepository;

    public ApplicationConfiguration(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountRepository accountRepository() {
        return accountRepository;
    }
}
