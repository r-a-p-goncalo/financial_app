package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record GetFinancialContextRequest(
        FinancialContextId financialContextId,
        UserId userId
) {
}
