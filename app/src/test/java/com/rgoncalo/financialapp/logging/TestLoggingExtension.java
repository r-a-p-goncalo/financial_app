package com.rgoncalo.financialapp.logging;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.MDC;

public class TestLoggingExtension
        implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(
            ExtensionContext context
    ) {

        MDC.put(
                "testClass",
                context
                        .getRequiredTestClass()
                        .getSimpleName()
        );

        MDC.put(
                "test",
                context
                        .getRequiredTestMethod()
                        .getName()
        );
    }

    @Override
    public void afterEach(
            ExtensionContext context
    ) {

        MDC.clear();
    }
}
