package com.rgoncalo.financialapp.application.transaction;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record ListTransactionsSummaryRequest(
        FinancialContextId financialContextId,
        UserId userId
) {
}
