package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

public record CreateAccountRequest(
        String name,
        MonetaryValue initialAmount,
        String financialContextId
) {}