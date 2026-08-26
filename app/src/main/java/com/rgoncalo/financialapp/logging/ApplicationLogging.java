package com.rgoncalo.financialapp.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Prepares the log destination before Logback creates its first logger.
 */
public final class ApplicationLogging {

    public static final String LOG_FILE_PROPERTY =
            "financial.app.log.file";

    private ApplicationLogging() {
    }

    /**
     * Places a fresh log file next to the supplied database file.
     */
    public static void configureForDatabase(Path databaseFile) {
        Objects.requireNonNull(databaseFile);

        Path absoluteDatabaseFile = databaseFile.toAbsolutePath();
        Path directory = absoluteDatabaseFile.getParent();
        Path logFile = directory.resolve(
                logFileNameFor(absoluteDatabaseFile.getFileName().toString())
        );

        try {
            Files.createDirectories(directory);
            Files.deleteIfExists(logFile);
            Files.createFile(logFile);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not prepare application log file: " + logFile,
                    exception
            );
        }

        System.setProperty(LOG_FILE_PROPERTY, logFile.toString());
    }

    private static String logFileNameFor(String databaseFileName) {
        int extensionStart = databaseFileName.lastIndexOf('.');
        String baseName = extensionStart > 0
                ? databaseFileName.substring(0, extensionStart)
                : databaseFileName;

        return baseName + ".log";
    }
}
