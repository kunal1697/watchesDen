package com.aeon.order.dto;

import com.aeon.order.model.Order;
import com.aeon.order.model.OrderItem;
import com.aeon.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record ShippingAddress(
            @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 100) String fullName,
            @Schema(example = "221B Baker Street") @NotBlank @Size(max = 200) String addressLine,
            @Schema(example = "London") @NotBlank @Size(max = 80) String city,
            @Schema(example = "NW16XE") @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{3,10}$", message = "invalid postal code")
            String postalCode,
            @Schema(example = "United Kingdom") @NotBlank @Size(max = 60) String country) {
    }

    /**
     * Payment is simulated: no card data is ever collected or stored.
     */
    public record CheckoutRequest(
            @NotNull @Valid ShippingAddress shipping,
            @Schema(example = "CARD", allowableValues = {"CARD", "UPI", "COD"})
            @NotBlank @Pattern(regexp = "CARD|UPI|COD", message = "must be CARD, UPI or COD") String paymentMethod) {
    }

    public record StatusUpdateRequest(@Schema(example = "SHIPPED") @NotNull OrderStatus status) {
    }

    public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity,
                                    BigDecimal lineTotal, List<String> optionIds, String customizationSummary) {
        static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                    i.getLineTotal(), i.getOptionIds(), i.getCustomizationSummary());
        }
    }

    public record OrderResponse(Long id, String orderNumber, Long userId, OrderStatus status, BigDecimal totalAmount,
                                String paymentMethod, ShippingAddress shipping, List<OrderItemResponse> items,
                                Instant createdAt, Instant updatedAt) {
        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getOrderNumber(), o.getUserId(), o.getStatus(), o.getTotalAmount(),
                    o.getPaymentMethod(),
                    new ShippingAddress(o.getShippingName(), o.getShippingAddress(), o.getShippingCity(),
                            o.getShippingPostalCode(), o.getShippingCountry()),
                    o.getItems().stream().map(OrderItemResponse::from).toList(),
                    o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
