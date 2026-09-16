package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;

import java.util.UUID;

public class CreateTransaction {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FinancialContextAuthorization authorization;

    public CreateTransaction(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            FinancialContextAuthorization authorization
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.authorization = authorization;
    }

    /**
     * Creates a transaction from the supplied request.
     *
     * @param request data required to create the transaction
     * @return the newly created transaction
     */
    public TransactionRecord execute(CreateTransactionRequest request) {
        authorization.requirePermission(
                request.userId(),
                request.financialContextId(),
                FinancialContextPermission.WRITE
        );

        if (request.originAccountId() != null
                && request.originAccountId().equals(
                request.targetAccountId()
        )) {
            throw new IllegalArgumentException(
                    "Origin and target accounts cannot be the same."
            );
        }

        if (request.originAccountId() == null && request.targetAccountId() == null)
            throw new IllegalArgumentException("Request must have an origin or a target");

        if (request.originAccountId() != null && !request.financialContextId()
                .equals(request.originAccountId().financialContextId())) {
            throw new IllegalArgumentException(
                    "Origin account must belong to the financial context."
            );
        }

        if (request.targetAccountId() != null && !request.financialContextId()
                .equals(request.targetAccountId().financialContextId())) {
            throw new IllegalArgumentException(
                    "Target account must belong to the financial context."
            );
        }

        if (request.originAccountId() != null && accountRepository.findById(request.originAccountId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Origin account does not exist in the financial context."
            );
        }

        if (request.targetAccountId() != null && accountRepository.findById(request.targetAccountId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Target account does not exist in the financial context."
            );
        }

        String transactionId = UUID.randomUUID().toString();

        TransactionRecord transaction = new TransactionRecord(
                new TransactionRecordId(
                        transactionId,
                        request.financialContextId()
                ),
                request.originAccountId(),
                request.targetAccountId(),
                request.dateTime(),
                request.value()
        );

        return transactionRepository.save(transaction);
    }
}
