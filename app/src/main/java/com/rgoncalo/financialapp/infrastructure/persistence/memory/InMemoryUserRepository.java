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
}
