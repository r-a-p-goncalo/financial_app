package com.rgoncalo.financialapp.bootstrap;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.security.AuthenticationException;
import com.rgoncalo.financialapp.application.user.AuthenticateUserRequest;
import com.rgoncalo.financialapp.application.user.CreateUserRequest;
import com.rgoncalo.financialapp.application.user.GetUserByNameRequest;
import com.rgoncalo.financialapp.commondata.user.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Adds a known scenario only when the explicitly opt-in bootstrap profile is
 * active. Commands are still executed through the application layer.
 */
@Component
@Profile("bootstrap")
public class BootstrapStartupRunner implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(BootstrapStartupRunner.class);

    private final Application application;
    private final BootstrapProperties properties;
    private final BootstrapConfigLoader configLoader;

    @Autowired
    public BootstrapStartupRunner(
            Application application,
            BootstrapProperties properties
    ) {
        this(application, properties, new BootstrapConfigLoader());
    }

    BootstrapStartupRunner(
            Application application,
            BootstrapProperties properties,
            BootstrapConfigLoader configLoader
    ) {
        this.application = Objects.requireNonNull(application);
        this.properties = Objects.requireNonNull(properties);
        this.configLoader = Objects.requireNonNull(configLoader);
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments arguments) {
        if (!properties.enabled()) {
            logger.info("Bootstrap profile is active but startup data is disabled.");
            return;
        }

        Path configFile = configurationFile();
        BootstrapPlan plan = configLoader.load(configFile);
        BootstrapUser bootstrapUser = Objects.requireNonNull(
                plan.user(),
                "Bootstrap configuration requires a user."
        );
        UserRecord user = resolveUser(bootstrapUser);

        new BootstrapRunner(application, user.userId()).run(plan);
        logger.info(
                "Bootstrap scenario {} is ready for user {}.",
                configFile.toAbsolutePath(),
                user.name()
        );
    }

    private Path configurationFile() {
        if (properties.configFile() == null
                || properties.configFile().isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap configuration file is required when startup data is enabled."
            );
        }

        return Path.of(properties.configFile());
    }

    private UserRecord resolveUser(BootstrapUser bootstrapUser) {
        return application.getUserByName().execute(
                        new GetUserByNameRequest(bootstrapUser.name())
                )
                .map(existing -> authenticateExistingUser(
                        existing,
                        bootstrapUser
                ))
                .orElseGet(() -> application.createUser().execute(
                        new CreateUserRequest(
                                bootstrapUser.name(),
                                bootstrapUser.password()
                        )
                ));
    }

    private UserRecord authenticateExistingUser(
            UserRecord existing,
            BootstrapUser bootstrapUser
    ) {
        try {
            return application.authenticateUser().execute(
                    new AuthenticateUserRequest(
                            bootstrapUser.name(),
                            bootstrapUser.password()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new IllegalStateException(
                    "Bootstrap user "
                            + existing.name()
                            + " exists but does not accept the configured password. "
                            + "Reset the isolated database or use another bootstrap user.",
                    exception
            );
        }
    }
}
