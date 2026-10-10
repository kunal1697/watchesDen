package com.aeon.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Internal contract between order-service and product-service. Not routed by the gateway.
 */
public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record ReserveItem(@NotNull Long productId, @Min(1) @Max(10) int quantity, List<String> optionIds) {
        public List<String> safeOptionIds() {
            return optionIds == null ? List.of() : optionIds;
        }
    }

    public record ReserveRequest(@NotEmpty List<@Valid ReserveItem> items) {
    }

    public record ReservedLine(Long productId, String productName, int quantity, BigDecimal unitPrice,
                               List<String> optionIds, String customizationSummary, boolean available) {
    }

    public record ReserveResponse(List<ReservedLine> lines) {
    }

    public record ReleaseItem(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record ReleaseRequest(@NotEmpty List<@Valid ReleaseItem> items) {
    }

    /** Price quote without reserving stock — used to show live cart totals. */
    public record QuoteResponse(List<ReservedLine> lines) {
    }
}
