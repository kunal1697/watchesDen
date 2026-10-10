package com.aeon.common.config;

import com.aeon.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * Provides a {@link JwtService} to any service (or the gateway) that sets {@code aeon.jwt.secret}.
 */
@AutoConfiguration
@ConditionalOnProperty("aeon.jwt.secret")
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(@Value("${aeon.jwt.secret}") String secret,
                                 @Value("${aeon.jwt.ttl:PT24H}") Duration ttl) {
        return new JwtService(secret, ttl);
    }
}
