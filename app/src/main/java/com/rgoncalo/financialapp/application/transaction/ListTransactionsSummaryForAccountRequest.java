package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;

public record ListTransactionsSummaryForAccountRequest(
        AccountRecordId accountRecordId
) {
}
