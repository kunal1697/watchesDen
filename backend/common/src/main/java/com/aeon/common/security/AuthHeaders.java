package com.aeon.common.security;

import com.aeon.common.exception.UnauthorizedException;

public final class AuthHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private AuthHeaders() {
    }

    public static void requireAdmin(String role) {
        if (!Roles.ADMIN.equals(role)) {
            throw UnauthorizedException.forbidden("Admin role required");
        }
    }

    public static Long requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("Authentication required");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid user identity");
        }
    }
}
