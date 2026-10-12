package com.aeon.user.controller;

import com.aeon.common.security.AuthHeaders;
import com.aeon.user.dto.AuthDtos.AuthResponse;
import com.aeon.user.dto.AuthDtos.LoginRequest;
import com.aeon.user.dto.AuthDtos.RegisterRequest;
import com.aeon.user.dto.AuthDtos.UserResponse;
import com.aeon.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth & Users", description = "Registration, login and profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer account and receive a JWT")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/api/auth/login")
    @Operation(summary = "Log in with email and password and receive a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/api/users/me")
    @Operation(summary = "Current user's profile (requires Bearer token via the gateway)")
    public UserResponse me(@Parameter(hidden = true) @RequestHeader(AuthHeaders.USER_ID) String userId) {
        return authService.profile(AuthHeaders.requireUser(userId));
    }
}
