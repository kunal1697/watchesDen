package com.aeon.user.config;

import com.aeon.common.security.Roles;
import com.aeon.user.model.User;
import com.aeon.user.repository.UserRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserConfig {

    private static final Logger log = LoggerFactory.getLogger(UserConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Seeds the first admin account so the admin console is usable out of the box. */
    @Bean
    public ApplicationRunner adminSeeder(UserRepository users, PasswordEncoder encoder,
                                         @Value("${aeon.admin.email}") String email,
                                         @Value("${aeon.admin.password}") String password) {
        return args -> {
            if (!users.existsByEmailIgnoreCase(email)) {
                users.save(new User(email, encoder.encode(password), "WatchesDen Admin", Roles.ADMIN));
                log.info("Seeded admin account {}", email);
            }
        };
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("WatchesDen user-service").version("1.0.0")
                        .description("Registration, login and profile. Call through the API gateway."))
                .addServersItem(new Server().url("/").description("API gateway"));
    }
}
