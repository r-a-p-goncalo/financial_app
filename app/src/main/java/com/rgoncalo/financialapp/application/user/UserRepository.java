package com.rgoncalo.financialapp.application.user;

import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.util.Collection;
import java.util.Optional;

/**
 * Persistence boundary for application users.
 */
public interface UserRepository {

    UserRecord save(UserRecord user);

    Collection<UserRecord> listUsersSummary();

    Optional<UserRecord> findById(UserId userId);
}
