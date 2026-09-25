package com.rgoncalo.financialapp.application.user;

import java.util.Optional;

/**
 * Selects the strategy used for new passwords and resolves legacy hashes.
 *
 * TODO: Check if this is really needed at this moment for the app, even if it seems like a good long-term strategy
 */
public interface PasswordHashingStrategyResolver {

    PasswordHashingStrategy current();

    Optional<PasswordHashingStrategy> findById(String strategyId);
}
