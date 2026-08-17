package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FinancialContextSummaryTest {

    @Test
    void returnsSummariesProvidedByRepository() {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository();
        List<FinancialContextRecord> expected = List.of(
                new FinancialContextRecord("context-1", "Personal"),
                new FinancialContextRecord("context-2", "Household")
        );
        repository.setSummaryResult(expected);

        FinancialContextSummary useCase =
                new FinancialContextSummary(repository);

        var result = useCase.execute(new FinancialContextSummaryRequest());

        assertEquals(1, repository.summaryCalls());
        assertSame(expected, result);
    }
}
