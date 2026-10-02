package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecordId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record SoftDeleteTransactionRequest(
        FinancialContextId financialContextId,
        TransactionRecordId transactionRecordId,
        UserId userId
) {
}
