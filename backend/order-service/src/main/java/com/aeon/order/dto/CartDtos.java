package com.aeon.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddItemRequest(
            @Schema(example = "1") @NotNull Long productId,
            @Schema(example = "1") @Min(1) @Max(10) int quantity,
            @Schema(example = "[\"case-titanium\",\"dial-aurora\",\"strap-mesh\",\"hands-ice\"]")
            @Size(max = 4) List<String> optionIds) {
    }

    public record UpdateQuantityRequest(@Schema(example = "2") @Min(1) @Max(10) int quantity) {
    }

    public record CartLine(Long id, Long productId, String productName, int quantity, BigDecimal unitPrice,
                           BigDecimal lineTotal, List<String> optionIds, String customizationSummary,
                           boolean available) {
    }

    public record CartResponse(List<CartLine> items, int itemCount, BigDecimal subtotal) {
    }
}
