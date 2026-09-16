package com.rgoncalo.financialapp.commondata.financialcontext;

/**
 * A user's permissions within one financial context.
 */
public enum FinancialContextPermission {
    READ,
    WRITE,
    OWNER;

    public boolean allows(FinancialContextPermission required) {
        return switch (this) {
            case READ -> required == READ;
            case WRITE -> required == READ || required == WRITE;
            case OWNER -> true;
        };
    }
}
