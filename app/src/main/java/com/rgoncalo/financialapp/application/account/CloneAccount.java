package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;

import java.util.UUID;

/**
 * Creates a real child account when a client explicitly promotes a virtual
 * inherited account before changing data that refers to it.
 */
public class CloneAccount {

    private final AccountRepository accountRepository;
    private final FinancialContextRepository financialContextRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public CloneAccount(
            AccountRepository accountRepository,
            FinancialContextRepository financialContextRepository,
            TransactionRepository transactionRepository,
            FinancialContextAuthorization authorization
    ) {
        this.accountRepository = accountRepository;
        this.financialContextRepository = financialContextRepository;
        this.transactionRepository = transactionRepository;
        this.authorization = authorization;
    }

    public AccountRecord execute(CloneAccountRequest request) {
        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.WRITE
        );

        EffectiveFinancialContext effectiveContext =
                new GetEffectiveFinancialContext(
                        financialContextRepository,
                        accountRepository,
                        transactionRepository,
                        authorization
                ).execute(new GetEffectiveFinancialContextRequest(
                        request.financialContextId(),
                        request.userId()
                )).orElseThrow(() -> new IllegalArgumentException(
                        "Financial context does not exist."
                ));

        AccountRecord source = effectiveContext.accounts().stream()
                .filter(account -> account.accountRecordId().equals(
                        request.sourceAccountRecordId()
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account does not exist in the financial context."
                ));

        if (request.financialContextId().equals(
                source.accountRecordId().financialContextId()
        )) {
            return source;
        }

        AccountRecord existingClone = accountRepository.listAccountsSummary(
                request.financialContextId()
        ).stream().filter(account -> request.sourceAccountRecordId().equals(
                account.parentAccountRecordId()
        )).findFirst().orElse(null);
        if (existingClone != null) {
            return existingClone;
        }

        AccountRecord clone = new AccountRecord(
                new AccountRecordId(
                        UUID.randomUUID().toString(),
                        request.financialContextId()
                ),
                source.name(),
                source.initialAmount(),
                source.accountRecordId(),
                0
        );

        return accountRepository.save(clone);
    }
}
