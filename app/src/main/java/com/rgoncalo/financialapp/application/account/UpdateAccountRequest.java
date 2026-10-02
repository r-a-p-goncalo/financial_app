package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record UpdateAccountRequest(
        FinancialContextId financialContextId,
        AccountRecordId accountRecordId,
        String name,
        MonetaryValue initialAmount,
        UserId userId
) {
}
