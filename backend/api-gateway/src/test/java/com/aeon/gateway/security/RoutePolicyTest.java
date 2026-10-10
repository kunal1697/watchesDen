package com.aeon.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class RoutePolicyTest {

    @Test
    void authEndpointsArePublic() {
        assertThat(RoutePolicy.isPublic(HttpMethod.POST, "/api/auth/login")).isTrue();
        assertThat(RoutePolicy.isPublic(HttpMethod.POST, "/api/auth/register")).isTrue();
    }

    @Test
    void catalogIsPublicForReadsOnly() {
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/api/products")).isTrue();
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/api/products/7")).isTrue();
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/api/customizations")).isTrue();
        assertThat(RoutePolicy.isPublic(HttpMethod.POST, "/api/products")).isFalse();
    }

    @Test
    void cartAndOrdersNeedAuthentication() {
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/api/cart")).isFalse();
        assertThat(RoutePolicy.isPublic(HttpMethod.POST, "/api/orders/checkout")).isFalse();
    }

    @Test
    void swaggerIsPublic() {
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/swagger-ui.html")).isTrue();
        assertThat(RoutePolicy.isPublic(HttpMethod.GET, "/product-service/v3/api-docs")).isTrue();
    }

    @Test
    void adminPathsAreDetected() {
        assertThat(RoutePolicy.requiresAdmin("/api/admin/products/3")).isTrue();
        assertThat(RoutePolicy.requiresAdmin("/api/products/3")).isFalse();
    }
}
