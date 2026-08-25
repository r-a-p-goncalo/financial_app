package com.rgoncalo.financialapp.bootstrap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads a bootstrap plan from JSON and rejects unknown configuration fields.
 */
public class BootstrapConfigLoader {

    private final ObjectMapper objectMapper;

    public BootstrapConfigLoader() {
        this.objectMapper = new ObjectMapper()
                .enable(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
    }

    public BootstrapPlan load(Path configFile) {

        Objects.requireNonNull(configFile);

        try (var input = Files.newInputStream(configFile)) {
            return objectMapper.readValue(input, BootstrapPlan.class);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Could not load bootstrap configuration: "
                            + configFile.toAbsolutePath(),
                    exception
            );
        }
    }
}
