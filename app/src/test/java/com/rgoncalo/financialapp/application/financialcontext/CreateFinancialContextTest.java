package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RepositoryTestExtension.class)
class CreateFinancialContextTest {

    @TestTemplate
    void createsAndPersistsFinancialContextFromRequest(RepositoryTestConfiguration configuration) {

        RecordingFinancialContextRepository repository = new RecordingFinancialContextRepository(configuration.createFinancialContextRepository());
        CreateFinancialContext useCase = new CreateFinancialContext(repository);

        String name = "Personal";

        FinancialContextRecord result = useCase.execute(
                new CreateFinancialContextRequest(name)
        );

        assertEquals(1, repository.saveCalls());

        Optional<FinancialContextRecord> saved = repository.findById(result.id());
        assertFalse(saved.isEmpty());

        FinancialContextRecord savedContext = saved.get();

        assertNotNull(savedContext.id());
        assertFalse(savedContext.id().isBlank());
        assertEquals(name, savedContext.name());

        assertTrue(saved.get().equalsIdentity(result));
    }
}
