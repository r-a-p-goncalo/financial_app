package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.user.UserId;

public record GetAccountRequest(
        AccountRecordId accountRecordId,
        UserId userId
) {
}
