package com.aeon.common.security;

public record AuthUser(Long id, String email, String role) {

    public boolean isAdmin() {
        return Roles.ADMIN.equals(role);
    }
}
