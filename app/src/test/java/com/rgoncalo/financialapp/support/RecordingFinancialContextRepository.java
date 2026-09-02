package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class RecordingFinancialContextRepository implements FinancialContextRepository {

    private int saveCalls;
    private int findCalls;
    private int summaryCalls;
    private FinancialContextRepository financialContextRepository;

    public RecordingFinancialContextRepository(FinancialContextRepository financialContextRepository){
        this.financialContextRepository = financialContextRepository;
    }

    @Override
    public FinancialContextRecord save(FinancialContextRecord financialContext) {
        saveCalls++;
        return financialContextRepository.save(financialContext);
    }

    @Override
    public Collection<FinancialContextRecord> listFinancialContextsSummary() {
        summaryCalls++;
        return financialContextRepository.listFinancialContextsSummary();
    }

    @Override
    public Collection<FinancialContextRecord> listChildren(
            FinancialContextId parentFinancialContextId
    ) {
        return financialContextRepository.listChildren(
                parentFinancialContextId
        );
    }

    @Override
    public Optional<FinancialContextRecord> findById(FinancialContextId id) {
        findCalls++;
        return financialContextRepository.findById(id);
    }

    public int saveCalls() {
        return saveCalls;
    }

    public int findCalls() {
        return findCalls;
    }

    public int summaryCalls() {
        return summaryCalls;
    }
}
