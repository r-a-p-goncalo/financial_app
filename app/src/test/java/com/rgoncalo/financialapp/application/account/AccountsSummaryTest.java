package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AccountsSummaryTest {

    @Test
    void requestsSummariesForSpecifiedFinancialContext() {
        RecordingAccountRepository repository = new RecordingAccountRepository();
        List<AccountRecord> expected = List.of(
                new AccountRecord("account-1", "Checking", null, "context-1"),
                new AccountRecord("account-2", "Savings", null, "context-1")
        );
        repository.setSummaryResult(expected);

        AccountsSummary useCase = new AccountsSummary(repository);

        var result = useCase.execute(new AccountsSummaryRequest("context-1"));

        assertEquals(1, repository.summaryCalls());
        assertEquals("context-1", repository.requestedSummaryFinancialContextId());
        assertSame(expected, result);
    }
}
