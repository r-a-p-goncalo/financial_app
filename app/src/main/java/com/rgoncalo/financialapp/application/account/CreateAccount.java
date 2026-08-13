package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.account.Account;

import java.util.UUID;

public class CreateAccount {

    private final AccountRepository accountRepository;

    public CreateAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(CreateAccountRequest request) {

        String accountId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        Account account = new Account(
                accountId,
                request.name(),
                request.initialAmount()
        );

        return accountRepository.save(account);
    }
}