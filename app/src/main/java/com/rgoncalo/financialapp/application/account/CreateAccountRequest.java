package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record CreateAccountRequest(
        String name,
        MonetaryValue initialAmount,
        FinancialContextId financialContextId,
        UserId userId
) {}
