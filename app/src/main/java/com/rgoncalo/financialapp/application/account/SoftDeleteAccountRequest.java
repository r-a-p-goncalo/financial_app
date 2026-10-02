package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record SoftDeleteAccountRequest(
        FinancialContextId financialContextId,
        AccountRecordId accountRecordId,
        UserId userId
) {
}
