package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
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
 * Resolves a context without changing stored rows. Explicit child objects
 * precede inherited parent objects that have not been overridden.
 */
public class GetEffectiveFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialContextAuthorization authorization;

    public GetEffectiveFinancialContext(
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

    public Optional<EffectiveFinancialContext> execute(
            GetEffectiveFinancialContextRequest request
    ) {
        return resolve(
                request.financialContextId(),
                request.userId()
        );
    }

    /**
     *
     * Recursively resolves the effective financial context, computing the parents before the children
     *
     * Gets the currently selected context, storing its accounts and transactions
     *
     * If there is a parent context, starts resolving
     *
     * @param financialContextId
     * @param userId
     *
     * @return
     */
    private Optional<EffectiveFinancialContext> resolve(
            FinancialContextId financialContextId,
            UserId userId
    ) {

            authorization.requirePermission(
                    userId,
                    financialContextId,
                    FinancialContextPermission.READ
            );
            Optional<FinancialContextRecord> context =
                    financialContextRepository.findById(financialContextId);

            if (context.isEmpty()) { //if there is no context with that id
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
                    userId
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
                    accounts
            );

            return Optional.of(new EffectiveFinancialContext(
                    effectiveContext,
                    effectiveAccounts,
                    effectiveTransactions
            ));
    }

    /**
     *
     * Resolves the actual context data, such as the name
     *
     * This is to complete missing attributes that are defined in the parent
     */
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

    /**
     *
     * Resolves the actual accounts data
     *
     * This is to complete missing attributes that are defined in the parents
     */
    private List<AccountRecord> resolveAccounts(
            Collection<AccountRecord> accounts,
            Collection<AccountRecord> parentAccounts
    ) {
        Map<AccountRecordId, AccountRecord> parentAccountsById = new HashMap<>();

        for (AccountRecord parentAccount : parentAccounts) {
            parentAccountsById.put(parentAccount.accountRecordId(), parentAccount);
        }

        List<AccountRecord> effectiveAccounts = new ArrayList<>();
        Set<AccountRecordId> explicitlyDefinedParentAccountIds = new HashSet<>();

        for (AccountRecord account : accounts) {

            AccountRecord parentAccount = parentRecord(
                    account.parentAccountRecordId(),
                    parentAccountsById,
                    "account"
            );

            //note that this clones the account if parentAccount is null
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
            if (account.parentAccountRecordId() != null && !explicitlyDefinedParentAccountIds.add(account.parentAccountRecordId())) {
                throw new IllegalStateException(
                        "A child context cannot define the same account twice."
                );
            }
        }

        //for the parent accounts that had no children, simply had them to the effective list
        for (AccountRecord parentAccount : parentAccounts) {
            if (!explicitlyDefinedParentAccountIds.contains(
                    parentAccount.accountRecordId()
            )) {
                effectiveAccounts.add(parentAccount);
            }
        }

        return List.copyOf(effectiveAccounts);
    }

    private List<TransactionRecord> resolveTransactions(
            Collection<TransactionRecord> transactions,
            Collection<TransactionRecord> parentTransactions,
            Collection<AccountRecord> accounts
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
        for (AccountRecord account : accounts) {
            if (account.parentAccountRecordId() != null) {
                childAccountIdsByParentId.put(
                        account.parentAccountRecordId(),
                        account.accountRecordId()
                );
            }
        }

        List<TransactionRecord> effectiveTransactions = new ArrayList<>();
        Set<TransactionRecordId> explicitlyDefinedParentTransactionIds =
                new HashSet<>();

        for (TransactionRecord transaction : transactions) {
            if (transaction.parentTransactionRecordId() == null) {
                effectiveTransactions.add(transaction);
                continue;
            }

            TransactionRecord parentTransaction = parentRecord(
                    transaction.parentTransactionRecordId(),
                    parentTransactionsById,
                    "transaction"
            );
            if (!explicitlyDefinedParentTransactionIds.add(
                    transaction.parentTransactionRecordId()
            )) {
                throw new IllegalStateException(
                        "A child context cannot override the same transaction twice."
                );
            }
            effectiveTransactions.add(resolveChildTransaction(
                    transaction,
                    parentTransaction,
                    childAccountIdsByParentId
            ));
        }

        for (TransactionRecord parentTransaction : parentTransactions) {
            if (!explicitlyDefinedParentTransactionIds.contains(
                    parentTransaction.transactionRecordId()
            )) {
                effectiveTransactions.add(projectParentTransaction(
                        parentTransaction,
                        childAccountIdsByParentId
                ));
            }
        }

        return List.copyOf(effectiveTransactions);
    }

    private TransactionRecord projectParentTransaction(
            TransactionRecord parentTransaction,
            Map<AccountRecordId, AccountRecordId> childAccountIdsByParentId
    ) {
        return new TransactionRecord(
                parentTransaction.transactionRecordId(),
                childAccountId(
                        parentTransaction.originAccountId(),
                        childAccountIdsByParentId
                ),
                childAccountId(
                        parentTransaction.targetAccountId(),
                        childAccountIdsByParentId
                ),
                parentTransaction.dateTime(),
                parentTransaction.value(),
                parentTransaction.parentTransactionRecordId(),
                parentTransaction.overriddenAttributes()
        );
    }

    private TransactionRecord resolveChildTransaction(
            TransactionRecord childTransaction,
            TransactionRecord parentTransaction,
            Map<AccountRecordId, AccountRecordId> childAccountIdsByParentId
    ) {
        return new TransactionRecord(
                childTransaction.transactionRecordId(),
                childTransaction.inherits(TransactionRecord.Attribute.ORIGIN_ACCOUNT)
                        ? childAccountId(
                                parentTransaction.originAccountId(),
                                childAccountIdsByParentId
                        )
                        : childTransaction.originAccountId(),
                childTransaction.inherits(TransactionRecord.Attribute.TARGET_ACCOUNT)
                        ? childAccountId(
                                parentTransaction.targetAccountId(),
                                childAccountIdsByParentId
                        )
                        : childTransaction.targetAccountId(),
                childTransaction.inherits(TransactionRecord.Attribute.DATE_TIME)
                        ? parentTransaction.dateTime()
                        : childTransaction.dateTime(),
                childTransaction.inherits(TransactionRecord.Attribute.VALUE)
                        ? parentTransaction.value()
                        : childTransaction.value(),
                childTransaction.parentTransactionRecordId(),
                childTransaction.overriddenAttributes()
        );
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

        return childAccountIdsByParentId.getOrDefault(
                parentAccountId,
                parentAccountId
        );
    }
}
