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

/** Hides an account while preserving and detaching its derived accounts. */
public class SoftDeleteAccount {

    private static final int ALL_ATTRIBUTES = AccountRecord.Attribute.NAME.mask()
            | AccountRecord.Attribute.INITIAL_AMOUNT.mask();

    private final AccountRepository accountRepository;
    private final FinancialContextRepository financialContextRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public SoftDeleteAccount(
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

    public void execute(SoftDeleteAccountRequest request) {
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

        if (!request.financialContextId().equals(
                source.accountRecordId().financialContextId())) {
            // A child context must not delete its parent's account. Store a
            // tombstone that suppresses the inherited object only locally.
            accountRepository.save(new AccountRecord(
                    new AccountRecordId(UUID.randomUUID().toString(),
                            request.financialContextId()),
                    source.name(), source.initialAmount(), source.accountRecordId(),
                    ALL_ATTRIBUTES, false
            ));
            return;
        }

        for (AccountRecord child : accountRepository.listChildren(
                source.accountRecordId())) {
            if (child.live()) {
                accountRepository.save(new AccountRecord(
                        child.accountRecordId(),
                        child.inherits(AccountRecord.Attribute.NAME)
                                ? source.name() : child.name(),
                        child.inherits(AccountRecord.Attribute.INITIAL_AMOUNT)
                                ? source.initialAmount() : child.initialAmount(),
                        null, ALL_ATTRIBUTES
                ));
            }
        }

        AccountRecord stored = accountRepository.findById(source.accountRecordId())
                .orElseThrow();
        accountRepository.save(new AccountRecord(
                stored.accountRecordId(), stored.name(), stored.initialAmount(),
                stored.parentAccountRecordId(), stored.overriddenAttributes(), false
        ));
    }
}
