package com.aeon.common.security;

public final class AuthUser {

    private final Long id;
    private final String email;
    private final String role;

    public AuthUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long id() { return id; }
    public String email() { return email; }
    public String role() { return role; }

    public boolean isAdmin() {
        return Roles.ADMIN.equals(role);
    }
}
