package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a context-local lazy copy of a financial context.
 *
 * <p>The copied accounts and transactions have new identities in the child
 * context. Their parent links retain the source identity and their override
 * masks start empty, so all copied attributes inherit from their source.</p>
 */
public class CloneFinancialContext {

    private final FinancialContextRepository financialContextRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public CloneFinancialContext(
            FinancialContextRepository financialContextRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.financialContextRepository = financialContextRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public FinancialContextRecord execute(CloneFinancialContextRequest request) {
        FinancialContextRecord parent = financialContextRepository.findById(
                request.parentFinancialContextId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Parent financial context does not exist."
        ));

        FinancialContextId childId = new FinancialContextId(
                UUID.randomUUID().toString()
        );
        boolean overridesName = request.name() != null;
        FinancialContextRecord child = new FinancialContextRecord(
                childId,
                overridesName ? request.name() : parent.name(),
                parent.financialContextId(),
                overridesName
                        ? FinancialContextRecord.Attribute.NAME.mask()
                        : 0
        );

        financialContextRepository.save(child);

        Collection<AccountRecord> parentAccounts = accountRepository
                .listAccountsSummary(parent.financialContextId());
        Map<AccountRecordId, AccountRecordId> childAccountIds =
                cloneAccounts(parentAccounts, childId);

        cloneTransactions(
                transactionRepository.listTransactionsSummary(
                        parent.financialContextId()
                ),
                childId,
                childAccountIds
        );

        return child;
    }

    private Map<AccountRecordId, AccountRecordId> cloneAccounts(
            Collection<AccountRecord> parentAccounts,
            FinancialContextId childId
    ) {
        Map<AccountRecordId, AccountRecordId> childAccountIds =
                new LinkedHashMap<>();

        for (AccountRecord parentAccount : parentAccounts) {
            AccountRecordId childAccountId = new AccountRecordId(
                    UUID.randomUUID().toString(),
                    childId
            );
            AccountRecord childAccount = new AccountRecord(
                    childAccountId,
                    parentAccount.name(),
                    parentAccount.initialAmount(),
                    parentAccount.accountRecordId(),
                    0
            );

            accountRepository.save(childAccount);
            childAccountIds.put(
                    parentAccount.accountRecordId(),
                    childAccountId
            );
        }

        return childAccountIds;
    }

    private void cloneTransactions(
            Collection<TransactionRecord> parentTransactions,
            FinancialContextId childId,
            Map<AccountRecordId, AccountRecordId> childAccountIds
    ) {
        for (TransactionRecord parentTransaction : parentTransactions) {
            TransactionRecord childTransaction = new TransactionRecord(
                    new TransactionRecordId(UUID.randomUUID().toString(), childId),
                    childAccountId(
                            parentTransaction.originAccountId(),
                            childAccountIds
                    ),
                    childAccountId(
                            parentTransaction.targetAccountId(),
                            childAccountIds
                    ),
                    parentTransaction.dateTime(),
                    parentTransaction.value(),
                    parentTransaction.transactionRecordId(),
                    0
            );

            transactionRepository.save(childTransaction);
        }
    }

    private AccountRecordId childAccountId(
            AccountRecordId parentAccountId,
            Map<AccountRecordId, AccountRecordId> childAccountIds
    ) {
        if (parentAccountId == null) {
            return null;
        }

        AccountRecordId childAccountId = childAccountIds.get(parentAccountId);

        if (childAccountId == null) {
            throw new IllegalStateException(
                    "Transaction references an account outside its "
                            + "financial context."
            );
        }

        return childAccountId;
    }
}
