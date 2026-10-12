package com.aeon.product.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReserveItem {
        @NotNull
        private final Long productId;
        @Min(1) @Max(10)
        private final int quantity;
        private final List<String> optionIds;

        @JsonCreator
        public ReserveItem(@JsonProperty("productId") Long productId,
                           @JsonProperty("quantity") int quantity,
                           @JsonProperty("optionIds") List<String> optionIds) {
            this.productId = productId;
            this.quantity = quantity;
            this.optionIds = optionIds;
        }

        public Long productId() { return productId; }
        public int quantity() { return quantity; }
        public List<String> optionIds() { return optionIds; }

        public List<String> safeOptionIds() {
            return optionIds == null ? Collections.emptyList() : optionIds;
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReserveRequest {
        @NotEmpty
        private final List<@Valid ReserveItem> items;

        @JsonCreator
        public ReserveRequest(@JsonProperty("items") List<ReserveItem> items) {
            this.items = items;
        }

        public List<ReserveItem> items() { return items; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReservedLine {
        private final Long productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final List<String> optionIds;
        private final String customizationSummary;
        private final boolean available;

        public ReservedLine(Long productId, String productName, int quantity, BigDecimal unitPrice,
                            List<String> optionIds, String customizationSummary, boolean available) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.optionIds = optionIds;
            this.customizationSummary = customizationSummary;
            this.available = available;
        }

        public Long productId() { return productId; }
        public String productName() { return productName; }
        public int quantity() { return quantity; }
        public BigDecimal unitPrice() { return unitPrice; }
        public List<String> optionIds() { return optionIds; }
        public String customizationSummary() { return customizationSummary; }
        public boolean available() { return available; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReserveResponse {
        private final List<ReservedLine> lines;

        public ReserveResponse(List<ReservedLine> lines) {
            this.lines = lines;
        }

        public List<ReservedLine> lines() { return lines; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReleaseItem {
        @NotNull
        private final Long productId;
        @Min(1)
        private final int quantity;

        @JsonCreator
        public ReleaseItem(@JsonProperty("productId") Long productId, @JsonProperty("quantity") int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long productId() { return productId; }
        public int quantity() { return quantity; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ReleaseRequest {
        @NotEmpty
        private final List<@Valid ReleaseItem> items;

        @JsonCreator
        public ReleaseRequest(@JsonProperty("items") List<ReleaseItem> items) {
            this.items = items;
        }

        public List<ReleaseItem> items() { return items; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class QuoteResponse {
        private final List<ReservedLine> lines;

        public QuoteResponse(List<ReservedLine> lines) {
            this.lines = lines;
        }

        public List<ReservedLine> lines() { return lines; }
    }
}
