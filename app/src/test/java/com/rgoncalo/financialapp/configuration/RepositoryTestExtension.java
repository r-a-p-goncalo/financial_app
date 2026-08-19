package com.rgoncalo.financialapp.configuration;

import org.junit.jupiter.api.extension.*;

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

        return Stream.of(
                invocation("InMemory", InMemoryRepositoryTestConfiguration::new),
                invocation("SQLite", SQLiteTestConfiguration::new)
        );
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

                                return factory.get();
                            }
                        }
                );
            }
        };
    }
}
