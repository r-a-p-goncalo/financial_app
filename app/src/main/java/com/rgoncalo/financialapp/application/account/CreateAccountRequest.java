package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.money.MonetaryValue;

public record CreateAccountRequest(
        String name,
        MonetaryValue initialAmount
) {}