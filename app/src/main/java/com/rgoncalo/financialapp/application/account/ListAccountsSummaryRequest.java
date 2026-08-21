package com.rgoncalo.financialapp.application.account;


import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;

public record ListAccountsSummaryRequest(
        FinancialContextId financialContextId
) {}