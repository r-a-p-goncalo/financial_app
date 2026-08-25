package com.rgoncalo.financialapp.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BootstrapConfigLoaderTest {

    @Test
    void loadsTypedCommandsFromJson(@TempDir Path tempDirectory)
            throws IOException {

        Path file = tempDirectory.resolve("bootstrap.json");

        Files.writeString(
                file,
                """
                        {
                          "mode": "if-empty",
                          "commands": [
                            {
                              "type": "create-financial-context",
                              "ref": "personal",
                              "name": "Personal finances"
                            }
                          ]
                        }
                        """
        );

        BootstrapPlan result = new BootstrapConfigLoader().load(file);

        assertEquals(BootstrapMode.IF_EMPTY, result.mode());
        assertEquals(1, result.commands().size());
        assertInstanceOf(
                CreateFinancialContextBootstrapCommand.class,
                result.commands().get(0)
        );
    }
}
