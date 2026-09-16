package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.Collection;

/**
 * Application use case for listing accounts.
 *
 * <p></p>
 */
public class ListAccountsSummary {

    private final AccountRepository accountRepository;
    private final FinancialContextAuthorization authorization;

    public ListAccountsSummary(
            AccountRepository accountRepository,
            FinancialContextAuthorization authorization
    ) {
        this.accountRepository = accountRepository;
        this.authorization = authorization;
    }

    /**
     * Returns a list of accounts
     *
     * @param request, defines which accounts to return
     * @return a collection of accounts
     */
    public Collection<AccountRecord> execute(ListAccountsSummaryRequest request) {
        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.READ
        );

        return accountRepository.listAccountsSummary(request.financialContextId());
    }

}
