package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.cli.financialcontext.CommandCreateAccountCommand;
import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.infrastructure.database.memory.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CreateAccountCommandTest {

    private InMemoryAccountRepository repository;
    private CommandCreateAccountCommand command;

    @BeforeEach
    void setUp() {

        repository =
                new InMemoryAccountRepository();

        CreateAccount createAccount =
                new CreateAccount(repository);

        command =
                new CommandCreateAccountCommand(createAccount);
    }

    @Test
    void createsAccountFromUserInput() {

        Scanner scanner =
                new Scanner("""
                        Savings
                        250.75
                        """);

        command.execute(scanner);

        assertEquals(1, repository.size());

        Account account =
                repository.accounts()
                        .iterator()
                        .next();

        assertEquals(
                "Savings",
                account.getName()
        );

        assertEquals(
                0,
                new BigDecimal("250.75")
                        .compareTo(
                                account.getInitialAmount()
                                        .getValue()
                        )
        );
    }

    @Test
    void invalidAmountDoesNotCreateAccount() {

        Scanner scanner =
                new Scanner("""
                        Savings
                        not-a-number
                        """);

        assertDoesNotThrow(
                () -> command.execute(scanner)
        );

        assertEquals(0, repository.size());
    }

    @Test
    void trimsAccountName() {

        Scanner scanner =
                new Scanner("""
                           Savings Account
                        100
                        """);

        command.execute(scanner);

        Account account =
                repository.accounts()
                        .iterator()
                        .next();

        assertEquals(
                "Savings Account",
                account.getName()
        );
    }
}