package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record SoftDeleteFinancialContextRequest(
        FinancialContextId financialContextId,
        UserId userId
) {
}
