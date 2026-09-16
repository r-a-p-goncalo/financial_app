package com.rgoncalo.financialapp.commondata.user;

/**
 * Application-level user profile. It intentionally contains no credentials.
 */
public record UserRecord(UserId userId, String name) {

    public boolean equalsIdentity(Object object) {
        return object instanceof UserRecord other
                && userId.equals(other.userId);
    }
}
