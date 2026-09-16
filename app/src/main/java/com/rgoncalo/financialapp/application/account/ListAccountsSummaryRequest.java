package com.rgoncalo.financialapp.application.account;


import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record ListAccountsSummaryRequest(
        FinancialContextId financialContextId,
        UserId userId
) {}
