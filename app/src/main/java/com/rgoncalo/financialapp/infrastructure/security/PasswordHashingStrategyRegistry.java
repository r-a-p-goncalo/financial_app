package com.rgoncalo.financialapp.infrastructure.security;

import com.rgoncalo.financialapp.application.user.PasswordHashingStrategy;
import com.rgoncalo.financialapp.application.user.PasswordHashingStrategyResolver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry of the current password strategy and strategies retained to verify
 * existing user hashes.
 */
public class PasswordHashingStrategyRegistry
        implements PasswordHashingStrategyResolver {

    private final PasswordHashingStrategy current;
    private final Map<String, PasswordHashingStrategy> strategies;

    public PasswordHashingStrategyRegistry(
            PasswordHashingStrategy current,
            PasswordHashingStrategy... legacyStrategies
    ) {
        this.current = Objects.requireNonNull(current);
        this.strategies = new LinkedHashMap<>();
        register(current);

        for (PasswordHashingStrategy strategy : legacyStrategies) {
            register(strategy);
        }
    }

    @Override
    public PasswordHashingStrategy current() {
        return current;
    }

    @Override
    public Optional<PasswordHashingStrategy> findById(String strategyId) {
        return Optional.ofNullable(strategies.get(strategyId));
    }

    private void register(PasswordHashingStrategy strategy) {
        Objects.requireNonNull(strategy);
        PasswordHashingStrategy existing = strategies.putIfAbsent(
                strategy.id(),
                strategy
        );

        if (existing != null) {
            throw new IllegalArgumentException(
                    "Password hashing strategy is registered twice: "
                            + strategy.id()
            );
        }
    }
}
