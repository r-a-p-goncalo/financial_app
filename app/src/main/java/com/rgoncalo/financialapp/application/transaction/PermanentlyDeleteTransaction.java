package com.rgoncalo.financialapp.application.transaction;

/**
 * Internal maintenance operation. The REST API intentionally only exposes
 * {@link SoftDeleteTransaction}.
 */
public class PermanentlyDeleteTransaction {

    private final SoftDeleteTransaction softDeleteTransaction;
    private final TransactionRepository transactionRepository;

    public PermanentlyDeleteTransaction(
            SoftDeleteTransaction softDeleteTransaction,
            TransactionRepository transactionRepository
    ) {
        this.softDeleteTransaction = softDeleteTransaction;
        this.transactionRepository = transactionRepository;
    }

    public void execute(SoftDeleteTransactionRequest request) {
        softDeleteTransaction.execute(request);
        transactionRepository.deletePermanently(request.transactionRecordId());
    }
}
