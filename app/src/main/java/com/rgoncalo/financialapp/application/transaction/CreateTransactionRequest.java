package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.time.Instant;

public record CreateTransactionRequest (
        FinancialContextId financialContextId,
        AccountRecordId originAccountId,
        AccountRecordId targetAccountId,
        Instant dateTime,
        MonetaryValue value) {
}
