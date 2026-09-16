package com.rgoncalo.financialapp.commondata.user;

/**
 * A password hash together with the strategy that produced it.
 */
public record PasswordHash(String strategyId, String value) {
}
