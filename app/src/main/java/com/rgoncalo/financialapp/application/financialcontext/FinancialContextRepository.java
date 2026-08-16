package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.domain.financialcontext.FinancialContext;

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
     *
     * Queries a specific financial context
     *
     * @param id, the id of the financial context
     * @return the financial context with the id, possibly null
     */
    Optional<FinancialContextRecord> findById(String id);

}
