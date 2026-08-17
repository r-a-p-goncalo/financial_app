package com.rgoncalo.financialapp.support;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class RecordingFinancialContextRepository implements FinancialContextRepository {

    private FinancialContextRecord savedFinancialContext;
    private int saveCalls;
    private String requestedId;
    private int findCalls;
    private int summaryCalls;
    private Optional<FinancialContextRecord> findResult = Optional.empty();
    private Collection<FinancialContextRecord> summaryResult = List.of();

    @Override
    public FinancialContextRecord save(FinancialContextRecord financialContext) {
        savedFinancialContext = financialContext;
        saveCalls++;
        return financialContext;
    }

    @Override
    public Collection<FinancialContextRecord> listFinancialContextsSummary() {
        summaryCalls++;
        return summaryResult;
    }

    @Override
    public Optional<FinancialContextRecord> findById(String id) {
        requestedId = id;
        findCalls++;
        return findResult;
    }

    public FinancialContextRecord savedFinancialContext() {
        return savedFinancialContext;
    }

    public int saveCalls() {
        return saveCalls;
    }

    public String requestedId() {
        return requestedId;
    }

    public int findCalls() {
        return findCalls;
    }

    public int summaryCalls() {
        return summaryCalls;
    }

    public void setFindResult(Optional<FinancialContextRecord> findResult) {
        this.findResult = findResult;
    }

    public void setSummaryResult(Collection<FinancialContextRecord> summaryResult) {
        this.summaryResult = summaryResult;
    }
}
