package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.UUID;

/**
 * Application use case for creating an account in a financial context
 */
public class CreateAccount {

    private final AccountRepository accountRepository;
    private final FinancialContextAuthorization authorization;

    public CreateAccount(
            AccountRepository accountRepository,
            FinancialContextAuthorization authorization
    ) {
        this.accountRepository = accountRepository;
        this.authorization = authorization;
    }

    /**
     * Creates an account from the supplied request.
     *
     * @param request data required to create the account
     * @return the newly created account
     */
    public AccountRecord execute(CreateAccountRequest request) {
        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.WRITE
        );

        String accountId = UUID.randomUUID().toString(); // TODO: decide on the ID generation technique

        AccountRecord account = new AccountRecord(
                new AccountRecordId(accountId, request.financialContextId()),
                request.name(),
                request.initialAmount()
        );

        return accountRepository.save(account);
    }
}
