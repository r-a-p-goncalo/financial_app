package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetFinancialContextTest {

    @Test
    void requestsFinancialContextById() {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository();
        FinancialContextRecord expected =
                new FinancialContextRecord("context-1", "Personal");
        repository.setFindResult(Optional.of(expected));

        GetFinancialContext useCase = new GetFinancialContext(repository);

        Optional<FinancialContextRecord> result = useCase.execute(
                new GetFinancialContextRequest("context-1")
        );

        assertEquals(1, repository.findCalls());
        assertEquals("context-1", repository.requestedId());
        assertEquals(Optional.of(expected), result);
    }

    @Test
    void returnsEmptyWhenRepositoryDoesNotContainFinancialContext() {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository();
        repository.setFindResult(Optional.empty());

        GetFinancialContext useCase = new GetFinancialContext(repository);

        Optional<FinancialContextRecord> result = useCase.execute(
                new GetFinancialContextRequest("missing-context")
        );

        assertTrue(result.isEmpty());
        assertEquals("missing-context", repository.requestedId());
    }
}
