package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextAuthorization;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.commondata.user.UserId;

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

        if (request.originAccountId() == null && request.targetAccountId() == null)
            throw new IllegalArgumentException("Request must have an origin or a target");

        validateAccount(
                request.originAccountId(),
                request.financialContextId(),
                "Origin"
        );
        validateAccount(
                request.targetAccountId(),
                request.financialContextId(),
                "Target"
        );

        if (request.originAccountId() != null && request.originAccountId().equals(
                request.targetAccountId()
        )) {
            throw new IllegalArgumentException(
                    "Origin and target accounts cannot be the same."
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

    private void validateAccount(
            AccountRecordId accountId,
            FinancialContextId financialContextId,
            String side
    ) {
        if (accountId == null) {
            return;
        }
        if (!financialContextId.equals(accountId.financialContextId())) {
            throw new IllegalArgumentException(
                    side + " account must belong to the financial context."
            );
        }

        if (accountRepository.findById(accountId).isEmpty()) {
            throw new IllegalArgumentException(
                    side + " account does not exist in the financial context."
            );
        }
    }
}
