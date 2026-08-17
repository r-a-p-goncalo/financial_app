package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CreateFinancialContextTest {

    @Test
    void createsAndPersistsFinancialContextFromRequest() {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository();
        CreateFinancialContext useCase = new CreateFinancialContext(repository);

        FinancialContextRecord result = useCase.execute(
                new CreateFinancialContextRequest("Personal")
        );

        assertEquals(1, repository.saveCalls());

        FinancialContextRecord saved = repository.savedFinancialContext();
        assertNotNull(saved);
        assertNotNull(saved.id());
        assertFalse(saved.id().isBlank());
        assertEquals("Personal", saved.name());

        assertSame(saved, result);
    }
}
