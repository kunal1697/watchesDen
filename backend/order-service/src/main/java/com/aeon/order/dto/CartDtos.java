package com.aeon.order.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class AddItemRequest {
        @Schema(example = "1") @NotNull
        private final Long productId;
        @Schema(example = "1") @Min(1) @Max(10)
        private final int quantity;
        @Schema(example = "[\"case-titanium\",\"dial-aurora\",\"strap-mesh\",\"hands-ice\"]")
        @Size(max = 4)
        private final List<String> optionIds;

        @JsonCreator
        public AddItemRequest(@JsonProperty("productId") Long productId,
                              @JsonProperty("quantity") int quantity,
                              @JsonProperty("optionIds") List<String> optionIds) {
            this.productId = productId;
            this.quantity = quantity;
            this.optionIds = optionIds;
        }

        public Long productId() { return productId; }
        public int quantity() { return quantity; }
        public List<String> optionIds() { return optionIds; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class UpdateQuantityRequest {
        @Schema(example = "2") @Min(1) @Max(10)
        private final int quantity;

        @JsonCreator
        public UpdateQuantityRequest(@JsonProperty("quantity") int quantity) {
            this.quantity = quantity;
        }

        public int quantity() { return quantity; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class CartLine {
        private final Long id;
        private final Long productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;
        private final List<String> optionIds;
        private final String customizationSummary;
        private final boolean available;

        public CartLine(Long id, Long productId, String productName, int quantity, BigDecimal unitPrice,
                        BigDecimal lineTotal, List<String> optionIds, String customizationSummary,
                        boolean available) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
            this.optionIds = optionIds;
            this.customizationSummary = customizationSummary;
            this.available = available;
        }

        public Long id() { return id; }
        public Long productId() { return productId; }
        public String productName() { return productName; }
        public int quantity() { return quantity; }
        public BigDecimal unitPrice() { return unitPrice; }
        public BigDecimal lineTotal() { return lineTotal; }
        public List<String> optionIds() { return optionIds; }
        public String customizationSummary() { return customizationSummary; }
        public boolean available() { return available; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class CartResponse {
        private final List<CartLine> items;
        private final int itemCount;
        private final BigDecimal subtotal;

        public CartResponse(List<CartLine> items, int itemCount, BigDecimal subtotal) {
            this.items = items;
            this.itemCount = itemCount;
            this.subtotal = subtotal;
        }

        public List<CartLine> items() { return items; }
        public int itemCount() { return itemCount; }
        public BigDecimal subtotal() { return subtotal; }
    }
}
