package com.rgoncalo.financialapp.application.account;

import java.math.BigDecimal;

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
        BigDecimal initial_value
) {}
