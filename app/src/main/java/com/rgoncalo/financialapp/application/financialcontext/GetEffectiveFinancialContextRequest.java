package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

public record GetEffectiveFinancialContextRequest(
        FinancialContextId financialContextId
) {
}
