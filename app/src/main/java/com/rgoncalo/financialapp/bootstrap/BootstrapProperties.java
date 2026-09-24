package com.rgoncalo.financialapp.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Explicit controls for startup data in a disposable development environment.
 */
@ConfigurationProperties(prefix = "financial-app.bootstrap")
public record BootstrapProperties(boolean enabled, String configFile) {
}
