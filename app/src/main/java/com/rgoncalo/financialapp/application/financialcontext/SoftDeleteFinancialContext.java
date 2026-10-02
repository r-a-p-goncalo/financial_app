package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Soft-deletes a context and materializes every direct child first.  A child
 * can then continue independently even though its former parent is hidden.
 */
public class SoftDeleteFinancialContext {

    private static final int ALL_ACCOUNT_ATTRIBUTES =
            AccountRecord.Attribute.NAME.mask()
                    | AccountRecord.Attribute.INITIAL_AMOUNT.mask();
    private static final int ALL_TRANSACTION_ATTRIBUTES =
            TransactionRecord.Attribute.ORIGIN_ACCOUNT.mask()
                    | TransactionRecord.Attribute.TARGET_ACCOUNT.mask()
                    | TransactionRecord.Attribute.DATE_TIME.mask()
                    | TransactionRecord.Attribute.VALUE.mask();

    private final FinancialContextRepository financialContextRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public SoftDeleteFinancialContext(
            FinancialContextRepository financialContextRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            FinancialContextAuthorization authorization
    ) {
        this.financialContextRepository = financialContextRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authorization = authorization;
    }

    public void execute(SoftDeleteFinancialContextRequest request) {
        authorization.requirePermission(request.userId(), request.financialContextId(),
                FinancialContextPermission.WRITE);
        FinancialContextRecord context = financialContextRepository.findById(
                request.financialContextId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Financial context does not exist."
        ));

        for (FinancialContextRecord child : financialContextRepository.listChildren(
                context.financialContextId())) {
            materializeChild(child, request);
        }

        financialContextRepository.save(new FinancialContextRecord(
                context.financialContextId(), context.name(),
                context.parentFinancialContextId(), context.overriddenAttributes(), false
        ));
    }

    private void materializeChild(
            FinancialContextRecord child,
            SoftDeleteFinancialContextRequest request
    ) {
        EffectiveFinancialContext effectiveChild = new GetEffectiveFinancialContext(
                financialContextRepository, accountRepository, transactionRepository,
                authorization
        ).execute(new GetEffectiveFinancialContextRequest(
                child.financialContextId(), request.userId()
        )).orElseThrow(() -> new IllegalStateException(
                "Could not resolve child financial context."
        ));

        Map<AccountRecordId, AccountRecordId> childAccountIds = new HashMap<>();
        for (AccountRecord account : effectiveChild.accounts()) {
            AccountRecordId childAccountId = child.financialContextId().equals(
                    account.accountRecordId().financialContextId())
                    ? account.accountRecordId()
                    : new AccountRecordId(UUID.randomUUID().toString(),
                            child.financialContextId());
            childAccountIds.put(account.accountRecordId(), childAccountId);
            accountRepository.save(new AccountRecord(
                    childAccountId, account.name(), account.initialAmount(), null,
                    ALL_ACCOUNT_ATTRIBUTES
            ));
        }

        for (TransactionRecord transaction : effectiveChild.transactions()) {
            TransactionRecordId childTransactionId = child.financialContextId().equals(
                    transaction.transactionRecordId().financialContextId())
                    ? transaction.transactionRecordId()
                    : new TransactionRecordId(UUID.randomUUID().toString(),
                            child.financialContextId());
            transactionRepository.save(new TransactionRecord(
                    childTransactionId,
                    childAccountIds.getOrDefault(transaction.originAccountId(),
                            transaction.originAccountId()),
                    childAccountIds.getOrDefault(transaction.targetAccountId(),
                            transaction.targetAccountId()),
                    transaction.dateTime(), transaction.value(), null,
                    ALL_TRANSACTION_ATTRIBUTES
            ));
        }

        financialContextRepository.save(new FinancialContextRecord(
                child.financialContextId(), effectiveChild.financialContext().name(),
                null, FinancialContextRecord.Attribute.NAME.mask()
        ));
    }
}
