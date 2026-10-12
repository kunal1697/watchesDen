package com.aeon.order.client;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.logging.CorrelationId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * HTTP implementation over a load-balanced {@link RestTemplate} (Eureka resolves {@code product-service}).
 * Downstream 4xx errors are re-thrown with the same status and message so the customer sees e.g.
 * "Insufficient stock for Nebula Drift"; outages become 503.
 */
@Component
public class RestProductClient implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(RestProductClient.class);

    private final RestTemplate http;
    private final ObjectMapper json;

    public RestProductClient(RestTemplate productRestTemplate, ObjectMapper json) {
        this.http = productRestTemplate;
        this.json = json;
    }

    private static final class Lines {
        private final List<PricedLine> lines;

        @JsonCreator
        Lines(@JsonProperty("lines") List<PricedLine> lines) {
            this.lines = lines;
        }

        List<PricedLine> lines() {
            return lines == null ? Collections.emptyList() : lines;
        }
    }

    @Override
    public List<PricedLine> quote(List<LineRequest> lines) {
        return call(() -> http.postForObject("/internal/inventory/quote", request(lines), Lines.class)).lines();
    }

    @Override
    public List<PricedLine> reserve(List<LineRequest> lines) {
        return call(() -> http.postForObject("/internal/inventory/reserve", request(lines), Lines.class)).lines();
    }

    @Override
    public void release(List<ReleaseLine> lines) {
        call(() -> http.postForEntity("/internal/inventory/release", request(lines), Void.class));
    }

    private static HttpEntity<Map<String, Object>> request(List<?> lines) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CorrelationId.HEADER, correlationId());
        return new HttpEntity<>(Collections.singletonMap("items", lines), headers);
    }

    private <T> T call(Supplier<T> s) {
        try {
            return s.get();
        } catch (HttpStatusCodeException e) {
            throw downstream(e.getStatusCode(), e.getResponseBodyAsByteArray());
        } catch (ResourceAccessException e) {
            log.error("product-service unreachable: {}", e.getMessage());
            throw new BusinessException(503, "Product service is temporarily unavailable");
        }
    }

    private BusinessException downstream(HttpStatus status, byte[] body) {
        String message = "Product service error";
        try {
            JsonNode node = json.readTree(body);
            if (node != null && node.hasNonNull("message")) {
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
