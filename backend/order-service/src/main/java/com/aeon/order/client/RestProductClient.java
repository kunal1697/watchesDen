package com.aeon.order.client;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.logging.CorrelationId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * HTTP implementation over a load-balanced {@link RestClient} (Eureka resolves {@code product-service}).
 * Downstream 4xx errors are re-thrown with the same status and message so the customer sees e.g.
 * "Insufficient stock for Nebula Drift"; outages become 503.
 */
@Component
public class RestProductClient implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(RestProductClient.class);

    private final RestClient http;
    private final ObjectMapper json;

    public RestProductClient(RestClient productRestClient, ObjectMapper json) {
        this.http = productRestClient;
        this.json = json;
    }

    private record Lines(List<PricedLine> lines) {
    }

    @Override
    public List<PricedLine> quote(List<LineRequest> lines) {
        return call(() -> post("/internal/inventory/quote", lines).body(Lines.class)).lines();
    }

    @Override
    public List<PricedLine> reserve(List<LineRequest> lines) {
        return call(() -> post("/internal/inventory/reserve", lines).body(Lines.class)).lines();
    }

    @Override
    public void release(List<ReleaseLine> lines) {
        call(() -> http.post().uri("/internal/inventory/release")
                .header(CorrelationId.HEADER, correlationId())
                .body(Map.of("items", lines))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw downstream(res.getStatusCode(), res.getBody().readAllBytes());
                })
                .toBodilessEntity());
    }

    private RestClient.ResponseSpec post(String path, List<LineRequest> lines) {
        return http.post().uri(path)
                .header(CorrelationId.HEADER, correlationId())
                .body(Map.of("items", lines))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw downstream(res.getStatusCode(), res.getBody().readAllBytes());
                });
    }

    private <T> T call(Supplier<T> s) {
        try {
            return s.get();
        } catch (ResourceAccessException e) {
            log.error("product-service unreachable: {}", e.getMessage());
            throw new BusinessException(503, "Product service is temporarily unavailable");
        }
    }

    private BusinessException downstream(HttpStatusCode status, byte[] body) {
        String message = "Product service error";
        try {
            JsonNode node = json.readTree(body);
            if (node.hasNonNull("message")) {
                message = node.get("message").asText();
            }
        } catch (IOException ignored) {
            // keep default message
        }
        if (status.is5xxServerError()) {
            log.error("product-service returned {}: {}", status.value(), message);
            return new BusinessException(503, "Product service is temporarily unavailable");
        }
        return new BusinessException(status.value(), message);
    }

    private static String correlationId() {
        String id = MDC.get(CorrelationId.MDC_KEY);
        return id == null ? "" : id;
    }
}
