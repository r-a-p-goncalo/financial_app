package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RepositoryTestExtension.class)
class FinancialContextSummaryTest {

    @TestTemplate
    void requestsSummariesFromRepository(
            RepositoryTestConfiguration configuration
    ) {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository(
                        configuration.createFinancialContextRepository()
                );

        List<FinancialContextRecord> expected = List.of(
                new FinancialContextRecord("context-1", "Personal"),
                new FinancialContextRecord("context-2", "Household")
        );

        for (FinancialContextRecord context : expected) {
            repository.save(context);
        }

        FinancialContextSummary useCase =
                new FinancialContextSummary(repository);

        var result = useCase.execute(
                new FinancialContextSummaryRequest()
        );

        assertEquals(1, repository.summaryCalls());
        assertEquals(expected.size(), result.size());

        for (FinancialContextRecord expectedContext : expected) {
            assertTrue(
                    result.stream().anyMatch(expectedContext::equalsIdentity),
                    "Missing financial context: " + expectedContext
            );
        }
    }
}
