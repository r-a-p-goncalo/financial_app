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

/** Updates an account, promoting an inherited account into the target context. */
public class UpdateAccount {

    private final AccountRepository accountRepository;
    private final FinancialContextRepository financialContextRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public UpdateAccount(
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

    public AccountRecord execute(UpdateAccountRequest request) {
        authorization.requirePermission(request.userId(), request.financialContextId(),
                FinancialContextPermission.WRITE);

        EffectiveFinancialContext context = new GetEffectiveFinancialContext(
                financialContextRepository, accountRepository, transactionRepository,
                authorization
        ).execute(new GetEffectiveFinancialContextRequest(
                request.financialContextId(), request.userId()
        )).orElseThrow(() -> new IllegalArgumentException(
                "Financial context does not exist."
        ));

        AccountRecord source = context.accounts().stream()
                .filter(account -> account.accountRecordId().equals(
                        request.accountRecordId()
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account does not exist in the financial context."
                ));

        AccountRecordId targetId = request.financialContextId().equals(
                source.accountRecordId().financialContextId()
        ) ? source.accountRecordId() : new AccountRecordId(
                UUID.randomUUID().toString(), request.financialContextId()
        );
        AccountRecordId parentId = targetId.equals(source.accountRecordId())
                ? accountRepository.findById(targetId)
                        .map(AccountRecord::parentAccountRecordId).orElse(null)
                : source.accountRecordId();

        return accountRepository.save(new AccountRecord(
                targetId,
                request.name(),
                request.initialAmount(),
                parentId,
                AccountRecord.Attribute.NAME.mask()
                        | AccountRecord.Attribute.INITIAL_AMOUNT.mask()
        ));
    }
}
