package com.rgoncalo.financialapp.bootstrap;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.financialcontext.ListFinancialContextSummaryRequest;
import com.rgoncalo.financialapp.application.user.AuthenticateUserRequest;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryTransactionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.memory.InMemoryUserRepository;
import com.rgoncalo.financialapp.infrastructure.security.PasswordHashingStrategyRegistry;
import com.rgoncalo.financialapp.infrastructure.security.Pbkdf2PasswordHashingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BootstrapStartupRunnerTest {

    @Test
    void createsAndReusesTheKnownBootstrapUser(@TempDir Path tempDirectory)
            throws Exception {

        Path configFile = tempDirectory.resolve("financial-demo.json");
        Files.writeString(configFile, """
                {
                  "user": {
                    "name": "demo",
                    "password": "demo-password"
                  },
                  "mode": "if-empty",
                  "commands": [
                    {
                      "type": "create-financial-context",
                      "ref": "personal",
                      "name": "Personal finances"
                    }
                  ]
                }
                """);

        Application application = application();
        BootstrapStartupRunner runner = new BootstrapStartupRunner(
                application,
                new BootstrapProperties(true, configFile.toString())
        );

        runner.run(null);
        runner.run(null);

        var user = application.authenticateUser().execute(
                new AuthenticateUserRequest("demo", "demo-password")
        );

        assertEquals(
                1,
                application.listFinancialContextSummary().execute(
                        new ListFinancialContextSummaryRequest(user.userId())
                ).size()
        );
    }

    private Application application() {
        return new Application(new ApplicationConfiguration(
                new InMemoryAccountRepository(),
                new InMemoryFinancialContextRepository(),
                new InMemoryTransactionRepository(),
                new InMemoryUserRepository(),
                new InMemoryFinancialContextPermissionRepository(),
                new PasswordHashingStrategyRegistry(
                        new Pbkdf2PasswordHashingStrategy()
                )
        ));
    }
}
