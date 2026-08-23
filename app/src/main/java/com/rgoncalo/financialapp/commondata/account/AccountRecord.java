package com.rgoncalo.financialapp.commondata.account;

import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

/**
 * Represents the possibly partial data of an account.
 * Is used to support queries with partial information or hidden information mechanisms.
 *
 * @param name
 * @param initialAmount
 */
public record AccountRecord(
        AccountRecordId accountRecordId,
        String name,
        MonetaryValue initialAmount
) {

    /**
     *
     * Checks if 2 account records are of the same identity
     *
     * @param obj, the other account record
     *
     * @return true if they share the same id and financial context id
     */
    public boolean equalsIdentity(Object obj) {
        if(obj instanceof AccountRecord otherRecord)
            return this.accountRecordId.equals(otherRecord.accountRecordId);

        else
            return false;
    }
}
