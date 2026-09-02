package com.rgoncalo.financialapp.commondata.transaction;

import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.time.Instant;

public record TransactionRecord (TransactionRecordId transactionRecordId,
                                 AccountRecordId originAccountId,
                                 AccountRecordId targetAccountId,
                                 Instant dateTime,
                                 MonetaryValue value,
                                 TransactionRecordId parentTransactionRecordId,
                                 int overriddenAttributes) {

    public enum Attribute {
        ORIGIN_ACCOUNT(1),
        TARGET_ACCOUNT(1 << 1),
        DATE_TIME(1 << 2),
        VALUE(1 << 3);

        private final int mask;

        Attribute(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }
    }

    public TransactionRecord(
            TransactionRecordId transactionRecordId,
            AccountRecordId originAccountId,
            AccountRecordId targetAccountId,
            Instant dateTime,
            MonetaryValue value
    ) {
        this(transactionRecordId, originAccountId, targetAccountId, dateTime,
                value, null, 0);
    }

    public boolean overrides(Attribute attribute) {
        return (overriddenAttributes & attribute.mask()) != 0;
    }

    public boolean inherits(Attribute attribute) {
        return parentTransactionRecordId != null && !overrides(attribute);
    }

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
