package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String transactionId,
        String originAccountId,
        String targetAccountId,
        Instant dateTime,
        BigDecimal value
) {

    public static TransactionResponse from(TransactionRecord transaction) {
        return new TransactionResponse(
                transaction.transactionRecordId().transactionRecordId(),
                transaction.originAccountId() == null
                        ? null
                        : transaction.originAccountId().accountRecordId(),
                transaction.targetAccountId() == null
                        ? null
                        : transaction.targetAccountId().accountRecordId(),
                transaction.dateTime(),
                transaction.value().getValue()
        );
    }
}
