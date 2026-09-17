package com.rgoncalo.financialapp.commondata.user;

/**
 * Stable identity of an application user.
 *
 * <p>This identity is deliberately separate from authentication credentials.
 * Authentication adapters resolve a signed-in principal to this value without
 * changing the financial domain.</p>
 */
public record UserId(String userId) {
}
