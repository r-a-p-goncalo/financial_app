package com.rgoncalo.financialapp.infrastructure.persistence.memory;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.*;

public class InMemoryFinancialContextRepository implements FinancialContextRepository {

    private final Map<FinancialContextId, FinancialContextRecord> financialContexts = new LinkedHashMap<>();

    @Override
    public FinancialContextRecord save(FinancialContextRecord financialContext) {
        financialContexts.put(financialContext.financialContextId(), financialContext);
        return financialContext;
    }

    @Override
    public Collection<FinancialContextRecord> listFinancialContextsSummary() {
        return financialContexts.values();
    }

    @Override
    public Optional<FinancialContextRecord> findById(FinancialContextId id) {
        return Optional.ofNullable(financialContexts.get(id));
    }
}
