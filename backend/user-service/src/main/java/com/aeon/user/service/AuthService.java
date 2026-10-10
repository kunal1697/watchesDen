package com.aeon.user.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.common.exception.UnauthorizedException;
import com.aeon.common.security.JwtService;
import com.aeon.common.security.Roles;
import com.aeon.user.dto.AuthDtos.AuthResponse;
import com.aeon.user.dto.AuthDtos.LoginRequest;
import com.aeon.user.dto.AuthDtos.RegisterRequest;
import com.aeon.user.dto.AuthDtos.UserResponse;
import com.aeon.user.model.User;
import com.aeon.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("An account with this email already exists");
        }
        User user = users.save(new User(email, encoder.encode(req.password()), req.fullName().trim(), Roles.CUSTOMER));
        log.info("Registered user id={}", user.getId());
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt");
                    return new UnauthorizedException("Invalid email or password");
                });
        log.info("User id={} logged in", user.getId());
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public UserResponse profile(Long userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AuthResponse tokenFor(User user) {
        String token = jwt.issue(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwt.ttlSeconds(), UserResponse.from(user));
    }
}
