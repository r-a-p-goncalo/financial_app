package com.rgoncalo.financialapp.commondata.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.time.Instant;

public record TransactionRecord (TransactionRecordId transactionRecordId,
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
            return this.transactionRecordId.equals(otherRecord.transactionRecordId);

        else
            return false;
    }

}
