package com.aeon.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Decides which requests may pass the gateway without a token, and which need the ADMIN role.
 */
public final class RoutePolicy {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<String> PUBLIC_ANY_METHOD = List.of(
            "/api/auth/**",
            "/actuator/health",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**");

    private static final List<String> PUBLIC_GET = List.of(
            "/api/products",
            "/api/products/**",
            "/api/customizations",
            "/api/customizations/**");

    private static final String ADMIN = "/api/admin/**";

    private RoutePolicy() {
    }

    public static boolean isPublic(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        if (matchesAny(PUBLIC_ANY_METHOD, path)) {
            return true;
        }
        return HttpMethod.GET.equals(method) && matchesAny(PUBLIC_GET, path);
    }

    public static boolean requiresAdmin(String path) {
        return MATCHER.match(ADMIN, path);
    }

    private static boolean matchesAny(List<String> patterns, String path) {
        return patterns.stream().anyMatch(p -> MATCHER.match(p, path));
    }
}
