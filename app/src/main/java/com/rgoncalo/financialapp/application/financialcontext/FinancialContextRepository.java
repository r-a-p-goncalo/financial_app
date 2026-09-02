package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Collection;
import java.util.Optional;

public interface FinancialContextRepository {

    /**
     * Persists the supplied context.
     *
     * @param financialContext, financial context to persist
     */
    FinancialContextRecord save(FinancialContextRecord financialContext);

    /**
     *
     * @return the partial information for financial contexts available
     */
    Collection<FinancialContextRecord> listFinancialContextsSummary();

    /**
     * Returns the contexts copied directly from the supplied parent context.
     *
     * @param parentFinancialContextId the parent context identity
     * @return the direct children of the parent context
     */
    Collection<FinancialContextRecord> listChildren(
            FinancialContextId parentFinancialContextId
    );

    /**
     *
     * Queries a specific financial context
     *
     * @param id, the id of the financial context
     * @return the financial context with the id, possibly null
     */
    Optional<FinancialContextRecord> findById(FinancialContextId id);

}
