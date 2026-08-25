package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;

public record GetAccountRequest(
        AccountRecordId accountRecordId
) {
}
