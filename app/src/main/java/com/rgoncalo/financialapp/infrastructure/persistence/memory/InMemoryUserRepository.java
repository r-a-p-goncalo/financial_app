package com.rgoncalo.financialapp.infrastructure.persistence.memory;

import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, UserRecord> users = new LinkedHashMap<>();

    @Override
    public UserRecord save(UserRecord user) {
        boolean nameIsInUse = users.values().stream()
                .anyMatch(existing -> existing.name().equals(user.name())
                        && !existing.userId().equals(user.userId()));

        if (nameIsInUse) {
            throw new IllegalArgumentException(
                    "User name is already in use."
            );
        }

        users.put(user.userId(), user);
        return user;
    }

    @Override
    public Collection<UserRecord> listUsersSummary() {
        return List.copyOf(users.values());
    }

    @Override
    public Optional<UserRecord> findById(UserId userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<UserRecord> findByName(String name) {
        return users.values().stream()
                .filter(user -> user.name().equals(name))
                .findFirst();
    }
}
