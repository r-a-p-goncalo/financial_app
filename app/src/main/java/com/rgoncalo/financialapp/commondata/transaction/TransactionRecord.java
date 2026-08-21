package com.rgoncalo.financialapp.commondata.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.time.Instant;

public record TransactionRecord (TransactionRecordId id,
                                 AccountRecordId originAccountId,
                                 AccountRecordId targetAccountId,
                                 Instant dateTime,
                                 MonetaryValue value) {

    /**
     *
     * Checks if 2 financial account records are of the same identity
     *
     * @param obj, the other account record
     *
     * @return true if they share the same id
     */
    public boolean equalsIdentity(Object obj) {
        if(obj instanceof TransactionRecord otherRecord)
            return this.id.equals(otherRecord.id);

        else
            return false;
    }

}
