package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.Optional;

/**
 * Retrieves one account by identity.
 */
public class GetAccount {

    private final AccountRepository accountRepository;
    private final FinancialContextAuthorization authorization;

    public GetAccount(
            AccountRepository accountRepository,
            FinancialContextAuthorization authorization
    ) {
        this.accountRepository = accountRepository;
        this.authorization = authorization;
    }

    public Optional<AccountRecord> execute(
            GetAccountRequest request
    ) {
        authorization.requirePermission(
                request.userId(),
                request.accountRecordId().financialContextId(),
                FinancialContextPermission.READ
        );

        return accountRepository.findById(
                request.accountRecordId()
        );
    }
}
