package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RepositoryTestExtension.class)
class ListAccountsSummaryTest {

    @TestTemplate
    void requestsSummariesForSpecifiedFinancialContext(RepositoryTestConfiguration configuration){

        RecordingAccountRepository repository = new RecordingAccountRepository(configuration.createAccountRepository());

        List<AccountRecord> expected = List.of(
                new AccountRecord("account-1", "Checking", new MonetaryValue(0.0), "context-1"),
                new AccountRecord("account-2", "Savings", new MonetaryValue(0.0), "context-1")
        );

        for( AccountRecord accountRecord : expected){
            repository.save(accountRecord); //directly save in account record to skip the ID generation
        }

        ListAccountsSummary useCase = new ListAccountsSummary(repository);

        var result = useCase.execute(new ListAccountsSummaryRequest("context-1"));

        assertEquals(1, repository.summaryCalls());
        assertEquals(expected.size(), result.size());

        assertEquals(expected.size(), result.size());

        for (AccountRecord expectedAccount : expected) {
            assertTrue(
                    result.stream().anyMatch(expectedAccount::equalsIdentity),
                    "Missing account: " + expectedAccount
            );
        }
    }
}
