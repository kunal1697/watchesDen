package com.aeon.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class OrderConfig {

    @Bean
    @LoadBalanced
    public RestTemplate productRestTemplate(RestTemplateBuilder builder,
                                           @Value("${aeon.product-service.url:http://product-service}") String baseUrl) {
        return builder.rootUri(baseUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("WatchesDen order-service").version("1.0.0")
                        .description("Cart, checkout, order history and fulfilment."))
                .addServersItem(new Server().url("/").description("API gateway"))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
