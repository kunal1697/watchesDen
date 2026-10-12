package com.aeon.order.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * order-service's view of product-service's internal inventory API.
 */
public interface ProductClient {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    final class LineRequest {
        private final Long productId;
        private final int quantity;
        private final List<String> optionIds;

        public LineRequest(Long productId, int quantity, List<String> optionIds) {
            this.productId = productId;
            this.quantity = quantity;
            this.optionIds = optionIds;
        }

        public Long productId() { return productId; }
        public int quantity() { return quantity; }
        public List<String> optionIds() { return optionIds; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LineRequest)) return false;
            LineRequest that = (LineRequest) o;
            return quantity == that.quantity && Objects.equals(productId, that.productId)
                    && Objects.equals(optionIds, that.optionIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, quantity, optionIds);
        }

        @Override
        public String toString() {
            return "LineRequest[productId=" + productId + ", quantity=" + quantity + ", optionIds=" + optionIds + "]";
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    final class PricedLine {
        private final Long productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final List<String> optionIds;
        private final String customizationSummary;
        private final boolean available;

        @JsonCreator
        public PricedLine(@JsonProperty("productId") Long productId,
                          @JsonProperty("productName") String productName,
                          @JsonProperty("quantity") int quantity,
                          @JsonProperty("unitPrice") BigDecimal unitPrice,
                          @JsonProperty("optionIds") List<String> optionIds,
                          @JsonProperty("customizationSummary") String customizationSummary,
                          @JsonProperty("available") boolean available) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PricedLine)) return false;
            PricedLine that = (PricedLine) o;
            return quantity == that.quantity && available == that.available
                    && Objects.equals(productId, that.productId) && Objects.equals(productName, that.productName)
                    && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(optionIds, that.optionIds)
                    && Objects.equals(customizationSummary, that.customizationSummary);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, productName, quantity, unitPrice, optionIds, customizationSummary, available);
        }

        @Override
        public String toString() {
            return "PricedLine[productId=" + productId + ", productName=" + productName + ", quantity=" + quantity
                    + ", unitPrice=" + unitPrice + ", optionIds=" + optionIds
                    + ", customizationSummary=" + customizationSummary + ", available=" + available + "]";
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    final class ReleaseLine {
        private final Long productId;
        private final int quantity;

        public ReleaseLine(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long productId() { return productId; }
        public int quantity() { return quantity; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReleaseLine)) return false;
            ReleaseLine that = (ReleaseLine) o;
            return quantity == that.quantity && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, quantity);
        }

        @Override
        public String toString() {
            return "ReleaseLine[productId=" + productId + ", quantity=" + quantity + "]";
        }
    }

    /** Prices lines without reserving stock; result order matches request order. */
    List<PricedLine> quote(List<LineRequest> lines);

    /** Reserves stock for every line or throws; nothing is reserved on failure. */
    List<PricedLine> reserve(List<LineRequest> lines);

    void release(List<ReleaseLine> lines);
}
