package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;

import java.util.UUID;

/**
 * Application use case for creating an account.
 *
 * <p>This use case coordinates account creation and persistence</p>
 */
public class CreateAccount {

    private final AccountRepository accountRepository;

    public CreateAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates an account from the supplied request.
     *
     * @param request data required to create the account
     * @return the newly created account
     */
    public AccountRecord execute(CreateAccountRequest request) {

        String accountId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        AccountRecord account = new AccountRecord(
                accountId,
                request.name(),
                request.initialAmount(),
                request.financialContextId()
        );

        return accountRepository.save(account);
    }
}