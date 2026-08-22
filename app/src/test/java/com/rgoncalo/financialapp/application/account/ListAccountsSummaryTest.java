package com.rgoncalo.financialapp.application.account;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.configuration.RepositoryTestConfiguration;
import com.rgoncalo.financialapp.configuration.RepositoryTestExtension;
import com.rgoncalo.financialapp.logging.TestLoggingExtension;
import com.rgoncalo.financialapp.support.RecordingAccountRepository;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        RepositoryTestExtension.class,
        TestLoggingExtension.class // TODO: This should be in an outside configuration that extends automatically all test classes
})
class ListAccountsSummaryTest {

    @TestTemplate
    void requestsSummariesForSpecifiedFinancialContext(RepositoryTestConfiguration configuration){

        RecordingAccountRepository repository = new RecordingAccountRepository(configuration.createAccountRepository());

        FinancialContextId context1 = new FinancialContextId("context-1");

        AccountRecordId account1Id = new AccountRecordId("account-1", context1);
        AccountRecordId account2Id = new AccountRecordId("account-2", context1);


        List<AccountRecord> expected = List.of(
                new AccountRecord(account1Id, "Checking", new MonetaryValue(0.0)),
                new AccountRecord(account2Id, "Savings", new MonetaryValue(0.0))
        );

        for( AccountRecord accountRecord : expected){
            repository.save(accountRecord); //directly save in account record to skip the ID generation
        }

        ListAccountsSummary useCase = new ListAccountsSummary(repository);

        var result = useCase.execute(new ListAccountsSummaryRequest(context1));

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
