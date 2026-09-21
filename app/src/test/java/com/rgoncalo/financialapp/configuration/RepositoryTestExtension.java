package com.rgoncalo.financialapp.configuration;

import org.junit.jupiter.api.extension.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RepositoryTestExtension
        implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(
            ExtensionContext context) {

        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(
            ExtensionContext context) {

        List<TestTemplateInvocationContext> invocations = new ArrayList<>();
        invocations.add(invocation(
                "InMemory",
                InMemoryRepositoryTestConfiguration::new
        ));
        invocations.add(invocation("SQLite", SQLiteTestConfiguration::new));

        if (!Boolean.getBoolean(
                "financial-app.tests.skip-postgresql"
        )) {
            invocations.add(invocation(
                    "PostgreSQL",
                    PostgreSQLTestConfiguration::new
            ));
        }

        return invocations.stream();
    }

    private TestTemplateInvocationContext invocation(
            String name,
            Supplier<RepositoryTestConfiguration> factory) {

        return new TestTemplateInvocationContext() {

            @Override
            public String getDisplayName(int invocationIndex) {
                return name;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                RepositoryTestConfiguration configuration = factory.get();

                return List.of(
                        new ParameterResolver() {

                            @Override
                            public boolean supportsParameter(
                                    ParameterContext parameterContext,
                                    ExtensionContext extensionContext) {

                                return parameterContext
                                        .getParameter()
                                        .getType()
                                        .equals(
                                                RepositoryTestConfiguration.class
                                        );
                            }

                            @Override
                            public Object resolveParameter(
                                    ParameterContext parameterContext,
                                    ExtensionContext extensionContext) {

                                return configuration;
                            }
                        },
                        (AfterEachCallback) extensionContext -> {
                            try {
                                configuration.close();
                            } catch (Exception exception) {
                                throw new ExtensionConfigurationException(
                                        "Could not close repository test database.",
                                        exception
                                );
                            }
                        }
                );
            }
        };
    }
}
