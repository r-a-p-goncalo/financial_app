package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.user.UserId;

/**
 * Requests an explicit child copy of an inherited account.
 */
public record CloneAccountRequest(
        AccountRecordId sourceAccountRecordId,
        FinancialContextId financialContextId,
        UserId userId
) {
}
