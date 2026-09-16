package com.rgoncalo.financialapp.cli.user;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.security.AuthenticationException;
import com.rgoncalo.financialapp.application.user.AuthenticateUserRequest;
import com.rgoncalo.financialapp.application.user.CreateUserRequest;
import com.rgoncalo.financialapp.bootstrap.BootstrapPlan;
import com.rgoncalo.financialapp.bootstrap.BootstrapRunner;
import com.rgoncalo.financialapp.cli.usercontext.UserContextCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console entry point for registering and authenticating application users.
 */
public class UserAuthenticationCli {

    private final Scanner scanner;
    private final Application application;
    private final Optional<BootstrapPlan> bootstrapPlan;
    private boolean bootstrapRun;

    public UserAuthenticationCli(
            Scanner scanner,
            Application application,
            Optional<BootstrapPlan> bootstrapPlan
    ) {
        this.scanner = Objects.requireNonNull(scanner);
        this.application = Objects.requireNonNull(application);
        this.bootstrapPlan = Objects.requireNonNull(bootstrapPlan);
    }

    public void runCliLoop() {
        boolean toRun = true;

        System.out.println("Type 'help' to see available commands.");

        while (toRun) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();

            switch (command) {
                case "help" -> printHelp();
                case "register" -> register();
                case "login" -> login();
                case "exit" -> toRun = false;
                case "" -> {
                }
                default -> System.out.println("Unknown command: " + command);
            }
        }

        System.out.println("Goodbye.");
    }

    private void register() {
        System.out.println("Registering a user.");
        String name = read("User name: ");
        String password = readPassword("Password: ");
        String passwordConfirmation = readPassword("Confirm password: ");

        if (!password.equals(passwordConfirmation)) {
            System.out.println("Passwords do not match.");
            return;
        }

        UserRecord user;

        try {
            user = application.createUser().execute(
                    new CreateUserRequest(name, password)
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            return;
        }

        System.out.println("Created user " + user.name() + ".");
        openUserContext(user);
    }

    private void login() {
        System.out.println("Logging in.");
        String name = read("User name: ");
        String password = readPassword("Password: ");

        try {
            UserRecord user = application.authenticateUser().execute(
                    new AuthenticateUserRequest(name, password)
            );
            openUserContext(user);
        } catch (AuthenticationException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void openUserContext(UserRecord user) {
        runBootstrapOnce(user);
        new UserContextCli(
                scanner,
                new ClientApplication(application, user.userId())
        ).runCliLoop();
    }

    private void runBootstrapOnce(UserRecord user) {
        if (bootstrapRun) {
            return;
        }

        bootstrapPlan.ifPresent(plan -> new BootstrapRunner(
                application,
                user.userId()
        ).run(plan));
        bootstrapRun = true;
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readPassword(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void printHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  help            Show available commands");
        System.out.println("  register        Create a user and open its context");
        System.out.println("  login           Authenticate and open a user context");
        System.out.println("  exit            Exit the application");
        System.out.println();
    }
}
