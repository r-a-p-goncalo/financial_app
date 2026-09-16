package com.rgoncalo.financialapp.application.user;

import java.util.Optional;

/**
 * Selects the strategy used for new passwords and resolves legacy hashes.
 */
public interface PasswordHashingStrategyResolver {

    PasswordHashingStrategy current();

    Optional<PasswordHashingStrategy> findById(String strategyId);
}
