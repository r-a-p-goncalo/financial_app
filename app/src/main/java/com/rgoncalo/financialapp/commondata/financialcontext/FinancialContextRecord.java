package com.rgoncalo.financialapp.commondata.financialcontext;


import com.rgoncalo.financialapp.commondata.account.AccountRecord;

/**
 * Represents the possibly partial data of an account.
 * Is used to support queries with partial information or hidden information mechanisms.
 *
 * @param name
 */
public record FinancialContextRecord(
        String id,
        String name
) {

    /**
     *
     * Checks if 2 financial account records are of the same identity
     *
     * @param obj, the other account record
     *
     * @return true if they share the same id
     */
    public boolean equalsIdentity(Object obj) {
        if(obj instanceof FinancialContextRecord otherRecord)
            return this.id.equals(otherRecord.id);

        else
            return false;
    }

}
