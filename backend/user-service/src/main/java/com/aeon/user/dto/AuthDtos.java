package com.aeon.user.dto;

import com.aeon.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 100) String fullName,
            @Schema(example = "ada@watchesden.com") @NotBlank @Email @Size(max = 150) String email,
            @Schema(example = "Secret@123")
            @NotBlank
            @Size(min = 8, max = 72, message = "must be 8-72 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain a letter and a digit")
            String password) {
    }

    public record LoginRequest(
            @Schema(example = "ada@watchesden.com") @NotBlank @Email String email,
            @Schema(example = "Secret@123") @NotBlank String password) {
    }

    public record UserResponse(Long id, String email, String fullName, String role, Instant createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getCreatedAt());
        }
    }

    public record AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {
    }
}
