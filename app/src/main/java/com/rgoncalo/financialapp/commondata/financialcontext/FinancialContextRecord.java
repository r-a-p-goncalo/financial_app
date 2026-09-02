package com.rgoncalo.financialapp.commondata.financialcontext;

/**
 * Represents the possibly partial data of an account.
 * Is used to support queries with partial information or hidden information mechanisms.
 *
 * @param name
 */
public record FinancialContextRecord(
        FinancialContextId financialContextId,
        String name,
        FinancialContextId parentFinancialContextId,
        int overriddenAttributes
) {

    public enum Attribute {
        NAME(1);

        private final int mask;

        Attribute(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }
    }

    public FinancialContextRecord(
            FinancialContextId financialContextId,
            String name
    ) {
        this(financialContextId, name, null, 0);
    }

    public boolean overrides(Attribute attribute) {
        return (overriddenAttributes & attribute.mask()) != 0;
    }

    public boolean inherits(Attribute attribute) {
        return parentFinancialContextId != null && !overrides(attribute);
    }

    /**
     *
     * Checks if 2 account records are of the same identity
     *
     * @param obj, the other account record
     *
     * @return true if they share the same id and financial context id
     */
    public boolean equalsIdentity(Object obj) {
        if(obj instanceof FinancialContextRecord otherRecord)
            return this.financialContextId.equals(otherRecord.financialContextId);

        else
            return false;
    }

}
