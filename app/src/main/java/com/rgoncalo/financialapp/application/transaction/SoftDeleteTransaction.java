package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.UUID;

/** Hides a transaction while preserving and detaching its derived records. */
public class SoftDeleteTransaction {

    private static final int ALL_ATTRIBUTES =
            TransactionRecord.Attribute.ORIGIN_ACCOUNT.mask()
                    | TransactionRecord.Attribute.TARGET_ACCOUNT.mask()
                    | TransactionRecord.Attribute.DATE_TIME.mask()
                    | TransactionRecord.Attribute.VALUE.mask();

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextAuthorization authorization;

    public SoftDeleteTransaction(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            FinancialContextRepository financialContextRepository,
            FinancialContextAuthorization authorization
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.financialContextRepository = financialContextRepository;
        this.authorization = authorization;
    }

    public void execute(SoftDeleteTransactionRequest request) {
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
        TransactionRecord source = context.transactions().stream()
                .filter(transaction -> transaction.transactionRecordId().equals(
                        request.transactionRecordId()
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction does not exist in the financial context."
                ));

        if (!request.financialContextId().equals(
                source.transactionRecordId().financialContextId())) {
            transactionRepository.save(new TransactionRecord(
                    new TransactionRecordId(UUID.randomUUID().toString(),
                            request.financialContextId()),
                    source.originAccountId(), source.targetAccountId(), source.dateTime(),
                    source.value(), source.transactionRecordId(), ALL_ATTRIBUTES, false
            ));
            return;
        }

        for (TransactionRecord child : transactionRepository.listChildren(
                source.transactionRecordId())) {
            if (child.live()) {
                transactionRepository.save(new TransactionRecord(
                        child.transactionRecordId(),
                        child.inherits(TransactionRecord.Attribute.ORIGIN_ACCOUNT)
                                ? source.originAccountId() : child.originAccountId(),
                        child.inherits(TransactionRecord.Attribute.TARGET_ACCOUNT)
                                ? source.targetAccountId() : child.targetAccountId(),
                        child.inherits(TransactionRecord.Attribute.DATE_TIME)
                                ? source.dateTime() : child.dateTime(),
                        child.inherits(TransactionRecord.Attribute.VALUE)
                                ? source.value() : child.value(),
                        null, ALL_ATTRIBUTES
                ));
            }
        }

        TransactionRecord stored = transactionRepository.findById(
                source.transactionRecordId()
        ).orElseThrow();
        transactionRepository.save(new TransactionRecord(
                stored.transactionRecordId(), stored.originAccountId(),
                stored.targetAccountId(), stored.dateTime(), stored.value(),
                stored.parentTransactionRecordId(), stored.overriddenAttributes(), false
        ));
    }
}
