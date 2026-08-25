package com.rgoncalo.financialapp.bootstrap;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.ListAccountsSummaryRequest;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummaryRequest;
import com.rgoncalo.financialapp.application.transaction.ListTransactionsSummaryRequest;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BootstrapRunnerTest {

    @Test
    void createsConfiguredDataOnceWhenTheDatabaseIsEmpty() {

        InMemoryAccountRepository accountRepository =
                new InMemoryAccountRepository();
        InMemoryFinancialContextRepository contextRepository =
                new InMemoryFinancialContextRepository();
        InMemoryTransactionRepository transactionRepository =
                new InMemoryTransactionRepository();
        Application application = new Application(
                new ApplicationConfiguration(
                        accountRepository,
                        contextRepository,
                        transactionRepository
                )
        );
        BootstrapPlan plan = new BootstrapPlan(
                BootstrapMode.IF_EMPTY,
                List.of(
                        new CreateFinancialContextBootstrapCommand(
                                "personal",
                                "Personal finances"
                        ),
                        new CreateAccountBootstrapCommand(
                                "personal",
                                "checking",
                                "Checking",
                                "1000.00"
                        ),
                        new CreateAccountBootstrapCommand(
                                "personal",
                                "savings",
                                "Savings",
                                "500.00"
                        ),
                        new CreateTransactionBootstrapCommand(
                                "personal",
                                "checking",
                                "savings",
                                "2026-08-25T09:00:00Z",
                                "100.00"
                        )
                )
        );

        BootstrapRunner runner = new BootstrapRunner(application);
        runner.run(plan);

        var contexts = application.listFinancialContextSummary().execute(
                new ListFinancialContextSummaryRequest()
        );

        assertEquals(1, contexts.size());

        var context = contexts.iterator().next();

        assertEquals(
                2,
                application.accountSummary().execute(
                        new ListAccountsSummaryRequest(
                                context.financialContextId()
                        )
                ).size()
        );
        assertEquals(
                1,
                application.transactionsSummary().execute(
                        new ListTransactionsSummaryRequest(
                                context.financialContextId()
                        )
                ).size()
        );

        runner.run(plan);

        assertEquals(
                1,
                application.listFinancialContextSummary().execute(
                        new ListFinancialContextSummaryRequest()
                ).size()
        );
    }
}
