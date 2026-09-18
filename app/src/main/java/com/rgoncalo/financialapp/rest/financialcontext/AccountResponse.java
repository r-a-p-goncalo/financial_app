package com.rgoncalo.financialapp.rest.financialcontext;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;

import java.math.BigDecimal;

public record AccountResponse(
        String accountId,
        String financialContextId,
        String name,
        BigDecimal initialAmount
) {

    public static AccountResponse from(AccountRecord account) {
        return new AccountResponse(
                account.accountRecordId().accountRecordId(),
                account.accountRecordId().financialContextId()
                        .financialContextId(),
                account.name(),
                account.initialAmount().getValue()
        );
    }
}
