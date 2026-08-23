package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class GetFinancialContextTest {

    @TestTemplate
    void requestsFinancialContextById( RepositoryTestConfiguration configuration ) {

        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository(configuration.createFinancialContextRepository());

        FinancialContextId contextId = new FinancialContextId("context-1");

        FinancialContextRecord expected =
                new FinancialContextRecord(contextId, "Personal");

        repository.save(expected);

        GetFinancialContext useCase = new GetFinancialContext(repository);

        Optional<FinancialContextRecord> result = useCase.execute(
                new GetFinancialContextRequest(contextId)
        );

        assertEquals(1, repository.findCalls());

        assertFalse(result.isEmpty());

        assertEquals(contextId, result.get().financialContextId());

        assertEquals(expected, result.get());
    }

    @TestTemplate
    void returnsEmptyWhenRepositoryDoesNotContainFinancialContext(RepositoryTestConfiguration configuration ) {

        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository(configuration.createFinancialContextRepository());

        GetFinancialContext useCase = new GetFinancialContext(repository);

        Optional<FinancialContextRecord> result = useCase.execute(
                new GetFinancialContextRequest(new FinancialContextId("missing-context"))
        );

        assertTrue(result.isEmpty());
    }
}
