package com.aeon.common.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!";

    @Test
    void issuedTokenVerifiesWithSameClaims() {
        JwtService jwt = new JwtService(SECRET, Duration.ofMinutes(5));

        String token = jwt.issue(42L, "neo@watchesden.com", Roles.ADMIN);

        assertThat(jwt.verify(token)).hasValueSatisfying(u -> {
            assertThat(u.id()).isEqualTo(42L);
            assertThat(u.email()).isEqualTo("neo@watchesden.com");
            assertThat(u.isAdmin()).isTrue();
        });
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, Duration.ofMinutes(5));
        String token = jwt.issue(1L, "a@b.c", Roles.CUSTOMER);

        assertThat(jwt.verify(token + "x")).isEmpty();
        assertThat(jwt.verify("not-a-jwt")).isEmpty();
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() {
        JwtService a = new JwtService(SECRET, Duration.ofMinutes(5));
        JwtService b = new JwtService(SECRET + "-different", Duration.ofMinutes(5));

        assertThat(b.verify(a.issue(1L, "a@b.c", Roles.CUSTOMER))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, Duration.ofSeconds(-1));

        assertThat(jwt.verify(jwt.issue(1L, "a@b.c", Roles.CUSTOMER))).isEmpty();
    }

    @Test
    void shortSecretIsRefused() {
        assertThatThrownBy(() -> new JwtService("short", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
