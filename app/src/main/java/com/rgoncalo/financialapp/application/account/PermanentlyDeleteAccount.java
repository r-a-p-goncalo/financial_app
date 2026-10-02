package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.transaction.PermanentlyDeleteTransaction;
import com.rgoncalo.financialapp.application.transaction.SoftDeleteTransactionRequest;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

/**
 * Internal maintenance operation. It removes the local transaction history
 * that references the account after descendants have been detached.
 */
public class PermanentlyDeleteAccount {

    private final SoftDeleteAccount softDeleteAccount;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PermanentlyDeleteTransaction permanentlyDeleteTransaction;

    public PermanentlyDeleteAccount(
            SoftDeleteAccount softDeleteAccount,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PermanentlyDeleteTransaction permanentlyDeleteTransaction
    ) {
        this.softDeleteAccount = softDeleteAccount;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.permanentlyDeleteTransaction = permanentlyDeleteTransaction;
    }

    public void execute(SoftDeleteAccountRequest request) {
        if (!request.financialContextId().equals(
                request.accountRecordId().financialContextId())) {
            throw new IllegalArgumentException(
                    "An inherited account can only be permanently deleted in its source context."
            );
        }
        softDeleteAccount.execute(request);
        for (TransactionRecord transaction : transactionRepository
                .listStoredTransactions(request.financialContextId())) {
            if (request.accountRecordId().equals(transaction.originAccountId())
                    || request.accountRecordId().equals(transaction.targetAccountId())) {
                permanentlyDeleteTransaction.execute(
                        new SoftDeleteTransactionRequest(
                                request.financialContextId(),
                                transaction.transactionRecordId(), request.userId()
                        )
                );
            }
        }
        accountRepository.deletePermanently(request.accountRecordId());
    }
}
