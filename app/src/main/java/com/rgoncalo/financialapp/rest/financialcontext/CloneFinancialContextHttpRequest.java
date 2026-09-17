package com.rgoncalo.financialapp.rest.financialcontext;

/**
 * A null name continues to inherit the parent context name.
 */
public record CloneFinancialContextHttpRequest(String name) {
}
