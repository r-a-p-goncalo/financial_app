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
        return financialContexts.values().stream()
                .filter(FinancialContextRecord::live)
                .toList();
    }

    @Override
    public Collection<FinancialContextRecord> listChildren(
            FinancialContextId parentFinancialContextId
    ) {
        return financialContexts.values()
                .stream()
                .filter(context -> Objects.equals(
                        context.parentFinancialContextId(),
                        parentFinancialContextId
                ) && context.live())
                .toList();
    }

    @Override
    public Optional<FinancialContextRecord> findById(FinancialContextId id) {
        return Optional.ofNullable(financialContexts.get(id))
                .filter(FinancialContextRecord::live);
    }

    @Override
    public void deletePermanently(FinancialContextId id) {
        financialContexts.remove(id);
    }
}
