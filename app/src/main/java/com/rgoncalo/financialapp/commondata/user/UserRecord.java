package com.rgoncalo.financialapp.commondata.user;

/**
 * Application-level user profile and password-hash metadata.
 */
public record UserRecord(
        UserId userId,
        String name,
        PasswordHash passwordHash
) {

    public boolean equalsIdentity(Object object) {
        return object instanceof UserRecord other
                && userId.equals(other.userId);
    }
}
