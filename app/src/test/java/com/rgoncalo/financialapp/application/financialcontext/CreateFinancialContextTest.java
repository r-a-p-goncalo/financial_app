package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class CreateFinancialContextTest {

    @TestTemplate
    void createsAndPersistsFinancialContextFromRequest(RepositoryTestConfiguration configuration) {

        RecordingFinancialContextRepository repository = new RecordingFinancialContextRepository(configuration.createFinancialContextRepository());
        UserId userId = TestUsers.create(configuration, "alice");
        CreateFinancialContext useCase = new CreateFinancialContext(
                repository,
                configuration.createFinancialContextPermissionRepository(),
                configuration.createUserRepository()
        );

        String name = "Personal";

        FinancialContextRecord result = useCase.execute(
                new CreateFinancialContextRequest(name, userId)
        );

        assertEquals(1, repository.saveCalls());

        Optional<FinancialContextRecord> saved = repository.findById(result.financialContextId());
        assertFalse(saved.isEmpty());

        FinancialContextRecord savedContext = saved.get();

        assertNotNull(savedContext.financialContextId());
        assertEquals(name, savedContext.name());

        assertEquals(
                com.rgoncalo.financialapp.commondata.financialcontext
                        .FinancialContextPermission.OWNER,
                configuration.createFinancialContextPermissionRepository()
                        .findByUserAndContext(userId, result.financialContextId())
                        .orElseThrow()
                        .permission()
        );

        assertTrue(saved.get().equalsIdentity(result));
    }
}
