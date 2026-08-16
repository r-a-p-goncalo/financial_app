package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.domain.money.MonetaryValue;

/**
 * Represents the possibly partial data of an account.
 * Is used to support queries with partial information or hidden information mechanisms.
 *
 * @param name
 * @param initial_value
 */
public record AccountRecord(
        String id,
        String name,
        MonetaryValue initial_value,
        String financialContextId
) {}
