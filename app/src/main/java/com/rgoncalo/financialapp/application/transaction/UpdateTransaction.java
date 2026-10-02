package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.EffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContext;
import com.rgoncalo.financialapp.application.financialcontext.GetEffectiveFinancialContextRequest;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.UUID;

/** Updates a transaction, creating a local override when it is inherited. */
public class UpdateTransaction {

    private static final int ALL_ATTRIBUTES =
            TransactionRecord.Attribute.ORIGIN_ACCOUNT.mask()
                    | TransactionRecord.Attribute.TARGET_ACCOUNT.mask()
                    | TransactionRecord.Attribute.DATE_TIME.mask()
                    | TransactionRecord.Attribute.VALUE.mask();

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FinancialContextRepository financialContextRepository;
    private final FinancialContextAuthorization authorization;

    public UpdateTransaction(
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

    public TransactionRecord execute(UpdateTransactionRequest request) {
        authorization.requirePermission(request.userId(), request.financialContextId(),
                FinancialContextPermission.WRITE);
        validateAccounts(request.originAccountId(), request.targetAccountId(),
                request.financialContextId());

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

        boolean local = request.financialContextId().equals(
                source.transactionRecordId().financialContextId());
        TransactionRecordId targetId = local ? source.transactionRecordId()
                : new TransactionRecordId(UUID.randomUUID().toString(),
                        request.financialContextId());
        TransactionRecordId parentId = local
                ? transactionRepository.findById(targetId)
                        .map(TransactionRecord::parentTransactionRecordId).orElse(null)
                : source.transactionRecordId();

        return transactionRepository.save(new TransactionRecord(
                targetId,
                request.originAccountId(), request.targetAccountId(),
                request.dateTime(), request.value(), parentId, ALL_ATTRIBUTES
        ));
    }

    private void validateAccounts(
            AccountRecordId origin,
            AccountRecordId target,
            FinancialContextId financialContextId
    ) {
        if (origin == null && target == null) {
            throw new IllegalArgumentException(
                    "Request must have an origin or a target."
            );
        }
        validateAccount(origin, financialContextId, "Origin");
        validateAccount(target, financialContextId, "Target");
        if (origin != null && origin.equals(target)) {
            throw new IllegalArgumentException(
                    "Origin and target accounts cannot be the same."
            );
        }
    }

    private void validateAccount(
            AccountRecordId accountId,
            FinancialContextId financialContextId,
            String side
    ) {
        if (accountId == null) return;
        if (!financialContextId.equals(accountId.financialContextId())
                || accountRepository.findById(accountId).isEmpty()) {
            throw new IllegalArgumentException(
                    side + " account does not exist in the financial context."
            );
        }
    }
}
