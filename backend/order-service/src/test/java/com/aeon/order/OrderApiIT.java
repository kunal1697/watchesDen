package com.aeon.order;

import com.aeon.order.client.ProductClient;
import com.aeon.order.client.ProductClient.LineRequest;
import com.aeon.order.client.ProductClient.PricedLine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test of the order-service HTTP + persistence layers; product-service is stubbed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIT {

    private static final String CHECKOUT =
            "{\"shipping\":{\"fullName\":\"Ada\",\"addressLine\":\"1 Loop St\",\"city\":\"London\",\"postalCode\":\"NW1 6XE\",\"country\":\"UK\"},"
            + "\"paymentMethod\":\"CARD\"}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockBean
    private ProductClient productClient;

    @BeforeEach
    void stubProductService() {
        when(productClient.quote(anyList())).thenAnswer(inv -> priced(inv.getArgument(0)));
        when(productClient.reserve(anyList())).thenAnswer(inv -> priced(inv.getArgument(0)));
    }

    @Test
    void fullPurchaseJourney() throws Exception {
        String user = "101";
        mvc.perform(post("/api/cart/items").header("X-User-Id", user).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2,\"optionIds\":[\"case-titanium\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.subtotal").value(500.00));

        String cart = mvc.perform(post("/api/cart/items").header("X-User-Id", user).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":2,\"quantity\":1}"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        long secondLine = json.readTree(cart).get("items").get(1).get("id").asLong();

        mvc.perform(patch("/api/cart/items/" + secondLine).header("X-User-Id", user)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(jsonPath("$.itemCount").value(5));

        String order = mvc.perform(post("/api/orders/checkout").header("X-User-Id", user)
                        .header("Idempotency-Key", "journey-1")
                        .contentType(MediaType.APPLICATION_JSON).content(CHECKOUT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(1250.00))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(order).get("id").asLong();
        String orderNumber = json.readTree(order).get("orderNumber").asText();

        // Retry with the same key returns the same order and does not reserve again.
        mvc.perform(post("/api/orders/checkout").header("X-User-Id", user)
                        .header("Idempotency-Key", "journey-1")
                        .contentType(MediaType.APPLICATION_JSON).content(CHECKOUT))
                .andExpect(jsonPath("$.orderNumber").value(orderNumber));
        verify(productClient, times(1)).reserve(anyList());

        mvc.perform(get("/api/cart").header("X-User-Id", user))
                .andExpect(jsonPath("$.items.length()").value(0));

        mvc.perform(get("/api/orders").header("X-User-Id", user))
                .andExpect(jsonPath("$.content[0].orderNumber").value(orderNumber));

        mvc.perform(get("/api/orders/" + orderId).header("X-User-Id", "999"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/orders/" + orderId + "/cancel").header("X-User-Id", user))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        verify(productClient).release(anyList());
    }

    @Test
    void adminFulfilmentFlow() throws Exception {
        String user = "202";
        mvc.perform(post("/api/cart/items").header("X-User-Id", user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":3,\"quantity\":1}"));
        String order = mvc.perform(post("/api/orders/checkout").header("X-User-Id", user)
                        .contentType(MediaType.APPLICATION_JSON).content(CHECKOUT))
                .andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(order).get("id").asLong();

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isConflict());

        for (String s : List.of("CONFIRMED", "SHIPPED", "DELIVERED")) {
            mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("X-User-Role", "ADMIN")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + s + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(s));
        }

        mvc.perform(get("/api/admin/orders").param("status", "DELIVERED").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId));
    }

    @Test
    void checkoutEmptyCartAndValidation() throws Exception {
        mvc.perform(post("/api/orders/checkout").header("X-User-Id", "303")
                        .contentType(MediaType.APPLICATION_JSON).content(CHECKOUT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your cart is empty"));

        mvc.perform(post("/api/orders/checkout").header("X-User-Id", "303")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"shipping\":{},\"paymentMethod\":\"BITCOIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.paymentMethod").exists())
                .andExpect(jsonPath("$.fieldErrors['shipping.city']").exists());
    }

    @Test
    void cartRequiresIdentity() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/cart/items/1").header("X-User-Id", "404")).andExpect(status().isNotFound());
    }

    private static List<PricedLine> priced(List<LineRequest> lines) {
        return lines.stream().map(l -> new PricedLine(l.productId(), "Watch " + l.productId(), l.quantity(),
                new BigDecimal("250.00"), l.optionIds(), null, true)).collect(Collectors.toList());
    }
}
