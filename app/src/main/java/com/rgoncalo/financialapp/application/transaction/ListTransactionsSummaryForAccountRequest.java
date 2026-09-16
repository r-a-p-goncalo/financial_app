package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record ListTransactionsSummaryForAccountRequest(
        AccountRecordId accountRecordId,
        UserId userId
) {
}
