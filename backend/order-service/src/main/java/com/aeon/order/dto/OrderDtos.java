package com.aeon.order.dto;

import com.aeon.order.model.Order;
import com.aeon.order.model.OrderItem;
import com.aeon.order.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderDtos {

    private OrderDtos() {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ShippingAddress {
        @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 100)
        private final String fullName;
        @Schema(example = "221B Baker Street") @NotBlank @Size(max = 200)
        private final String addressLine;
        @Schema(example = "London") @NotBlank @Size(max = 80)
        private final String city;
        @Schema(example = "NW16XE") @NotBlank @Pattern(regexp = "^[A-Za-z0-9 -]{3,10}$", message = "invalid postal code")
        private final String postalCode;
        @Schema(example = "United Kingdom") @NotBlank @Size(max = 60)
        private final String country;

        @JsonCreator
        public ShippingAddress(@JsonProperty("fullName") String fullName,
                               @JsonProperty("addressLine") String addressLine,
                               @JsonProperty("city") String city,
                               @JsonProperty("postalCode") String postalCode,
                               @JsonProperty("country") String country) {
            this.fullName = fullName;
            this.addressLine = addressLine;
            this.city = city;
            this.postalCode = postalCode;
            this.country = country;
        }

        public String fullName() { return fullName; }
        public String addressLine() { return addressLine; }
        public String city() { return city; }
        public String postalCode() { return postalCode; }
        public String country() { return country; }
    }

    /**
     * Payment is simulated: no card data is ever collected or stored.
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class CheckoutRequest {
        @NotNull @Valid
        private final ShippingAddress shipping;
        @Schema(example = "CARD", allowableValues = {"CARD", "UPI", "COD"})
        @NotBlank @Pattern(regexp = "CARD|UPI|COD", message = "must be CARD, UPI or COD")
        private final String paymentMethod;

        @JsonCreator
        public CheckoutRequest(@JsonProperty("shipping") ShippingAddress shipping,
                               @JsonProperty("paymentMethod") String paymentMethod) {
            this.shipping = shipping;
            this.paymentMethod = paymentMethod;
        }

        public ShippingAddress shipping() { return shipping; }
        public String paymentMethod() { return paymentMethod; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class StatusUpdateRequest {
        @Schema(example = "SHIPPED") @NotNull
        private final OrderStatus status;

        @JsonCreator
        public StatusUpdateRequest(@JsonProperty("status") OrderStatus status) {
            this.status = status;
        }

        public OrderStatus status() { return status; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class OrderItemResponse {
        private final Long productId;
        private final String productName;
        private final BigDecimal unitPrice;
        private final int quantity;
        private final BigDecimal lineTotal;
        private final List<String> optionIds;
        private final String customizationSummary;

        public OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity,
                                 BigDecimal lineTotal, List<String> optionIds, String customizationSummary) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
            this.optionIds = optionIds;
            this.customizationSummary = customizationSummary;
        }

        static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                    i.getLineTotal(), i.getOptionIds(), i.getCustomizationSummary());
        }

        public Long productId() { return productId; }
        public String productName() { return productName; }
        public BigDecimal unitPrice() { return unitPrice; }
        public int quantity() { return quantity; }
        public BigDecimal lineTotal() { return lineTotal; }
        public List<String> optionIds() { return optionIds; }
        public String customizationSummary() { return customizationSummary; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class OrderResponse {
        private final Long id;
        private final String orderNumber;
        private final Long userId;
        private final OrderStatus status;
        private final BigDecimal totalAmount;
        private final String paymentMethod;
        private final ShippingAddress shipping;
        private final List<OrderItemResponse> items;
        private final Instant createdAt;
        private final Instant updatedAt;

        public OrderResponse(Long id, String orderNumber, Long userId, OrderStatus status, BigDecimal totalAmount,
                             String paymentMethod, ShippingAddress shipping, List<OrderItemResponse> items,
                             Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.orderNumber = orderNumber;
            this.userId = userId;
            this.status = status;
            this.totalAmount = totalAmount;
            this.paymentMethod = paymentMethod;
            this.shipping = shipping;
            this.items = items;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getOrderNumber(), o.getUserId(), o.getStatus(), o.getTotalAmount(),
                    o.getPaymentMethod(),
                    new ShippingAddress(o.getShippingName(), o.getShippingAddress(), o.getShippingCity(),
                            o.getShippingPostalCode(), o.getShippingCountry()),
                    o.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList()),
                    o.getCreatedAt(), o.getUpdatedAt());
        }

        public Long id() { return id; }
        public String orderNumber() { return orderNumber; }
        public Long userId() { return userId; }
        public OrderStatus status() { return status; }
        public BigDecimal totalAmount() { return totalAmount; }
        public String paymentMethod() { return paymentMethod; }
        public ShippingAddress shipping() { return shipping; }
        public List<OrderItemResponse> items() { return items; }
        public Instant createdAt() { return createdAt; }
        public Instant updatedAt() { return updatedAt; }
    }
}
