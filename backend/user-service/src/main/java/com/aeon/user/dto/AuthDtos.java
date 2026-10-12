package com.aeon.user.dto;

import com.aeon.user.model.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class RegisterRequest {
        @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 100)
        private final String fullName;
        @Schema(example = "ada@watchesden.com") @NotBlank @Email @Size(max = 150)
        private final String email;
        @Schema(example = "Secret@123")
        @NotBlank
        @Size(min = 8, max = 72, message = "must be 8-72 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain a letter and a digit")
        private final String password;

        @JsonCreator
        public RegisterRequest(@JsonProperty("fullName") String fullName,
                               @JsonProperty("email") String email,
                               @JsonProperty("password") String password) {
            this.fullName = fullName;
            this.email = email;
            this.password = password;
        }

        public String fullName() { return fullName; }
        public String email() { return email; }
        public String password() { return password; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class LoginRequest {
        @Schema(example = "ada@watchesden.com") @NotBlank @Email
        private final String email;
        @Schema(example = "Secret@123") @NotBlank
        private final String password;

        @JsonCreator
        public LoginRequest(@JsonProperty("email") String email, @JsonProperty("password") String password) {
            this.email = email;
            this.password = password;
        }

        public String email() { return email; }
        public String password() { return password; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class UserResponse {
        private final Long id;
        private final String email;
        private final String fullName;
        private final String role;
        private final Instant createdAt;

        public UserResponse(Long id, String email, String fullName, String role, Instant createdAt) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.createdAt = createdAt;
        }

        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getCreatedAt());
        }

        public Long id() { return id; }
        public String email() { return email; }
        public String fullName() { return fullName; }
        public String role() { return role; }
        public Instant createdAt() { return createdAt; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class AuthResponse {
        private final String token;
        private final String tokenType;
        private final long expiresIn;
        private final UserResponse user;

        public AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {
            this.token = token;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.user = user;
        }

        public String token() { return token; }
        public String tokenType() { return tokenType; }
        public long expiresIn() { return expiresIn; }
        public UserResponse user() { return user; }
    }
}
