package com.rgoncalo.financialapp.application.financialcontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingFinancialContextRepository;
import com.rgoncalo.financialapp.support.TestUsers;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.user.UserId;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})class ListFinancialContextSummaryTest {

    @TestTemplate
    void requestsSummariesFromRepository(
            RepositoryTestConfiguration configuration
    ) {
        RecordingFinancialContextRepository repository =
                new RecordingFinancialContextRepository(
                        configuration.createFinancialContextRepository()
                );

        FinancialContextId context1 = new FinancialContextId("context-1");
        FinancialContextId context2 = new FinancialContextId("context-2");

        List<FinancialContextRecord> expected = List.of(
                new FinancialContextRecord(context1, "Personal"),
                new FinancialContextRecord(context2, "Household")
        );

        for (FinancialContextRecord context : expected) {
            repository.save(context);
        }
        repository.save(new FinancialContextRecord(
                new FinancialContextId("private-context"),
                "Private"
        ));

        UserId userId = TestUsers.create(configuration, "alice");
        TestUsers.grant(configuration, userId, context1,
                FinancialContextPermission.READ);
        TestUsers.grant(configuration, userId, context2,
                FinancialContextPermission.WRITE);

        ListFinancialContextSummary useCase =
                new ListFinancialContextSummary(
                        repository,
                        configuration.createFinancialContextPermissionRepository()
                );

        var result = useCase.execute(
                new ListFinancialContextSummaryRequest(userId)
        );

        assertEquals(0, repository.summaryCalls());
        assertEquals(expected.size(), result.size());

        for (FinancialContextRecord expectedContext : expected) {
            assertTrue(
                    result.stream().anyMatch(expectedContext::equalsIdentity),
                    "Missing financial context: " + expectedContext
            );
        }
    }
}
