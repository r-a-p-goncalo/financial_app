package com.rgoncalo.financialapp.commondata.account;

import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

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
