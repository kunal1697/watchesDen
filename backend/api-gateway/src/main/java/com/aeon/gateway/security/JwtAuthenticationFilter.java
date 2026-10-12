package com.aeon.gateway.security;

import com.aeon.common.logging.CorrelationId;
import com.aeon.common.security.AuthHeaders;
import com.aeon.common.security.AuthUser;
import com.aeon.common.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Validates the bearer token on every request, strips spoofed identity headers, and forwards the
 * authenticated identity to downstream services as {@code X-User-*} headers.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String correlationId = Optional.ofNullable(request.getHeaders().getFirst(CorrelationId.HEADER))
                .orElseGet(() -> UUID.randomUUID().toString());

        Optional<AuthUser> user = extractToken(request).flatMap(jwtService::verify);

        if (user.isEmpty() && !RoutePolicy.isPublic(request.getMethod(), path)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Authentication required", correlationId);
        }
        if (RoutePolicy.requiresAdmin(path) && user.map(u -> !u.isAdmin()).orElse(true)) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Admin role required", correlationId);
        }

        ServerHttpRequest mutated = request.mutate().headers(h -> {
            h.remove(AuthHeaders.USER_ID);
            h.remove(AuthHeaders.USER_EMAIL);
            h.remove(AuthHeaders.USER_ROLE);
            h.set(CorrelationId.HEADER, correlationId);
            user.ifPresent(u -> {
                h.set(AuthHeaders.USER_ID, String.valueOf(u.id()));
                h.set(AuthHeaders.USER_EMAIL, u.email());
                h.set(AuthHeaders.USER_ROLE, u.role());
            });
        }).build();

        exchange.getResponse().getHeaders().set(CorrelationId.HEADER, correlationId);
        log.debug("[{}] {} {} user={}", correlationId, request.getMethod(), path, user.map(AuthUser::id).orElse(null));
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Optional<String> extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7).trim());
        }
        return Optional.empty();
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message, String correlationId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(CorrelationId.HEADER, correlationId);
        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"correlationId\":\"%s\"}",
                Instant.now(), status.value(), status.getReasonPhrase(), message,
                exchange.getRequest().getPath().value(), correlationId);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
