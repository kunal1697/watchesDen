package com.aeon.user.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.common.exception.UnauthorizedException;
import com.aeon.common.security.JwtService;
import com.aeon.common.security.Roles;
import com.aeon.user.dto.AuthDtos.AuthResponse;
import com.aeon.user.dto.AuthDtos.LoginRequest;
import com.aeon.user.dto.AuthDtos.RegisterRequest;
import com.aeon.user.model.User;
import com.aeon.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("test-secret-key-that-is-at-least-32-bytes-long!", Duration.ofHours(1));
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, encoder, jwt);
    }

    @Test
    void registerHashesPasswordNormalisesEmailAndReturnsToken() {
        when(users.existsByEmailIgnoreCase("ada@watchesden.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(inv -> withId(inv.getArgument(0), 7L));

        AuthResponse res = service.register(new RegisterRequest(" Ada ", "  ADA@watchesden.com ", "Secret123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ada@watchesden.com");
        assertThat(saved.getValue().getFullName()).isEqualTo("Ada");
        assertThat(saved.getValue().getRole()).isEqualTo(Roles.CUSTOMER);
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Secret123");
        assertThat(encoder.matches("Secret123", saved.getValue().getPasswordHash())).isTrue();
        assertThat(jwt.verify(res.token())).hasValueSatisfying(u -> assertThat(u.id()).isEqualTo(7L));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("ada@watchesden.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("Ada", "ada@watchesden.com", "Secret123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
        verify(users, never()).save(any());
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        User u = withId(new User("ada@watchesden.com", encoder.encode("Secret123"), "Ada", Roles.CUSTOMER), 3L);
        when(users.findByEmailIgnoreCase("ada@watchesden.com")).thenReturn(Optional.of(u));

        AuthResponse res = service.login(new LoginRequest("ada@watchesden.com", "Secret123"));

        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.user().id()).isEqualTo(3L);
    }

    @Test
    void loginFailsWithWrongPassword() {
        User u = withId(new User("ada@watchesden.com", encoder.encode("Secret123"), "Ada", Roles.CUSTOMER), 3L);
        when(users.findByEmailIgnoreCase("ada@watchesden.com")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.login(new LoginRequest("ada@watchesden.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(users.findByEmailIgnoreCase("ghost@watchesden.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ghost@watchesden.com", "x")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void profileOfMissingUserIs404() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profile(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private static User withId(User u, Long id) {
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }
}
