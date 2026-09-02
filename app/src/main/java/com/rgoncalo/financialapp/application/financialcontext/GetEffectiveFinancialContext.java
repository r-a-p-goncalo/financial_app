package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the effective values of a context without changing stored rows.
 */
public class GetEffectiveFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetEffectiveFinancialContext(
            FinancialContextRepository financialContextRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.financialContextRepository = financialContextRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Optional<EffectiveFinancialContext> execute(
            GetEffectiveFinancialContextRequest request
    ) {
        return resolve(request.financialContextId(), new HashSet<>());
    }

    private Optional<EffectiveFinancialContext> resolve(
            FinancialContextId financialContextId,
            Set<FinancialContextId> visitedContextIds
    ) {
        if (!visitedContextIds.add(financialContextId)) {
            throw new IllegalStateException(
                    "Financial context inheritance contains a cycle."
            );
        }

        try {
            Optional<FinancialContextRecord> context =
                    financialContextRepository.findById(financialContextId);

            if (context.isEmpty()) {
                return Optional.empty();
            }

            Collection<AccountRecord> accounts = accountRepository
                    .listAccountsSummary(financialContextId);
            Collection<TransactionRecord> transactions = transactionRepository
                    .listTransactionsSummary(financialContextId);

            if (context.get().parentFinancialContextId() == null) {
                return Optional.of(new EffectiveFinancialContext(
                        context.get(),
                        accounts,
                        transactions
                ));
            }

            EffectiveFinancialContext parent = resolve(
                    context.get().parentFinancialContextId(),
                    visitedContextIds
            ).orElseThrow(() -> new IllegalStateException(
                    "Parent financial context does not exist."
            ));

            FinancialContextRecord effectiveContext = resolveContext(
                    context.get(),
                    parent.financialContext()
            );
            List<AccountRecord> effectiveAccounts = resolveAccounts(
                    accounts,
                    parent.accounts()
            );
            List<TransactionRecord> effectiveTransactions = resolveTransactions(
                    transactions,
                    parent.transactions(),
                    effectiveAccounts
            );

            return Optional.of(new EffectiveFinancialContext(
                    effectiveContext,
                    effectiveAccounts,
                    effectiveTransactions
            ));
        } finally {
            visitedContextIds.remove(financialContextId);
        }
    }

    private FinancialContextRecord resolveContext(
            FinancialContextRecord context,
            FinancialContextRecord parent
    ) {
        return new FinancialContextRecord(
                context.financialContextId(),
                context.inherits(FinancialContextRecord.Attribute.NAME)
                        ? parent.name()
                        : context.name(),
                context.parentFinancialContextId(),
                context.overriddenAttributes()
        );
    }

    private List<AccountRecord> resolveAccounts(
            Collection<AccountRecord> accounts,
            Collection<AccountRecord> parentAccounts
    ) {
        Map<AccountRecordId, AccountRecord> parentAccountsById = new HashMap<>();

        for (AccountRecord parentAccount : parentAccounts) {
            parentAccountsById.put(parentAccount.accountRecordId(), parentAccount);
        }

        List<AccountRecord> effectiveAccounts = new ArrayList<>();

        for (AccountRecord account : accounts) {
            AccountRecord parentAccount = parentRecord(
                    account.parentAccountRecordId(),
                    parentAccountsById,
                    "account"
            );
            effectiveAccounts.add(new AccountRecord(
                    account.accountRecordId(),
                    account.inherits(AccountRecord.Attribute.NAME)
                            ? parentAccount.name()
                            : account.name(),
                    account.inherits(AccountRecord.Attribute.INITIAL_AMOUNT)
                            ? parentAccount.initialAmount()
                            : account.initialAmount(),
                    account.parentAccountRecordId(),
                    account.overriddenAttributes()
            ));
        }

        return List.copyOf(effectiveAccounts);
    }

    private List<TransactionRecord> resolveTransactions(
            Collection<TransactionRecord> transactions,
            Collection<TransactionRecord> parentTransactions,
            Collection<AccountRecord> effectiveAccounts
    ) {
        Map<TransactionRecordId, TransactionRecord> parentTransactionsById =
                new HashMap<>();
        Map<AccountRecordId, AccountRecordId> childAccountIdsByParentId =
                new HashMap<>();

        for (TransactionRecord parentTransaction : parentTransactions) {
            parentTransactionsById.put(
                    parentTransaction.transactionRecordId(),
                    parentTransaction
            );
        }
        for (AccountRecord effectiveAccount : effectiveAccounts) {
            if (effectiveAccount.parentAccountRecordId() != null) {
                childAccountIdsByParentId.put(
                        effectiveAccount.parentAccountRecordId(),
                        effectiveAccount.accountRecordId()
                );
            }
        }

        List<TransactionRecord> effectiveTransactions = new ArrayList<>();

        for (TransactionRecord transaction : transactions) {
            TransactionRecord parentTransaction = parentRecord(
                    transaction.parentTransactionRecordId(),
                    parentTransactionsById,
                    "transaction"
            );
            effectiveTransactions.add(new TransactionRecord(
                    transaction.transactionRecordId(),
                    transaction.inherits(TransactionRecord.Attribute.ORIGIN_ACCOUNT)
                            ? childAccountId(
                                    parentTransaction.originAccountId(),
                                    childAccountIdsByParentId
                            )
                            : transaction.originAccountId(),
                    transaction.inherits(TransactionRecord.Attribute.TARGET_ACCOUNT)
                            ? childAccountId(
                                    parentTransaction.targetAccountId(),
                                    childAccountIdsByParentId
                            )
                            : transaction.targetAccountId(),
                    transaction.inherits(TransactionRecord.Attribute.DATE_TIME)
                            ? parentTransaction.dateTime()
                            : transaction.dateTime(),
                    transaction.inherits(TransactionRecord.Attribute.VALUE)
                            ? parentTransaction.value()
                            : transaction.value(),
                    transaction.parentTransactionRecordId(),
                    transaction.overriddenAttributes()
            ));
        }

        return List.copyOf(effectiveTransactions);
    }

    private <T, I> T parentRecord(
            I parentId,
            Map<I, T> parentRecords,
            String type
    ) {
        if (parentId == null) {
            return null;
        }

        T parentRecord = parentRecords.get(parentId);

        if (parentRecord == null) {
            throw new IllegalStateException(
                    "Could not resolve parent " + type + "."
            );
        }

        return parentRecord;
    }

    private AccountRecordId childAccountId(
            AccountRecordId parentAccountId,
            Map<AccountRecordId, AccountRecordId> childAccountIdsByParentId
    ) {
        if (parentAccountId == null) {
            return null;
        }

        AccountRecordId childAccountId = childAccountIdsByParentId.get(
                parentAccountId
        );

        if (childAccountId == null) {
            throw new IllegalStateException(
                    "Could not map inherited transaction account into the "
                            + "financial context."
            );
        }

        return childAccountId;
    }
}
