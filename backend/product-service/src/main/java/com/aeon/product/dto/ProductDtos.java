package com.aeon.product.dto;

import com.aeon.product.model.CustomizationOption;
import com.aeon.product.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ProductDtos {

    private static final String HEX = "^#[0-9A-Fa-f]{6}$";
    private static final String HEX_MSG = "must be a hex colour like #1A2B3C";
    private static final String URL = "^$|^https?://\\S+$";
    private static final String URL_MSG = "must be an http(s) URL";

    private ProductDtos() {
    }

    public record ProductRequest(
            @Schema(example = "Nebula Chrono X1") @NotBlank @Size(max = 120) String name,
            @Schema(example = "Nebula") @NotBlank @Size(max = 60) String collection,
            @Schema(example = "A chronograph forged for deep-space timekeeping.") @NotBlank @Size(max = 2000) String description,
            @Schema(example = "1299.00") @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Schema(example = "25") @Min(0) @Max(100000) int stock,
            @Schema(example = "Grade 5 Titanium") @NotBlank @Size(max = 60) String caseMaterial,
            @Schema(example = "Automatic") @NotBlank @Size(max = 60) String movement,
            @Schema(example = "42") @Min(20) @Max(60) int diameterMm,
            @Schema(example = "100") @Min(0) @Max(1000) int waterResistanceM,
            @Schema(example = "#C0C6CF") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG) String caseColor,
            @Schema(example = "#0B1026") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG) String dialColor,
            @Schema(example = "#1C1F2A") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG) String strapColor,
            @Schema(example = "#00F0FF") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG) String accentColor,
            @Schema(example = "https://images.unsplash.com/photo-1523170335258-f5ed11844a49")
            @Size(max = 500) @Pattern(regexp = URL, message = URL_MSG) String imageUrl,
            @Schema(example = "https://images.unsplash.com/photo-1727716919539-3cd4aaf7af99")
            @Size(max = 500) @Pattern(regexp = URL, message = URL_MSG) String hoverImageUrl,
            boolean customizable) {
    }

    public record ProductResponse(Long id, String name, String collection, String description, BigDecimal price,
                                  int stock, boolean inStock, String caseMaterial, String movement, int diameterMm,
                                  int waterResistanceM, String caseColor, String dialColor, String strapColor,
                                  String accentColor, String imageUrl, String hoverImageUrl,
                                  boolean customizable, boolean active,
                                  Instant createdAt, Instant updatedAt) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getCollection(), p.getDescription(), p.getPrice(),
                    p.getStock(), p.getStock() > 0, p.getCaseMaterial(), p.getMovement(), p.getDiameterMm(),
                    p.getWaterResistanceM(), p.getCaseColor(), p.getDialColor(), p.getStrapColor(),
                    p.getAccentColor(), p.getImageUrl(), p.getHoverImageUrl(), p.isCustomizable(), p.isActive(), p.getCreatedAt(), p.getUpdatedAt());
        }
    }

    public record OptionResponse(String id, String part, String name, String colorHex, BigDecimal priceDelta) {
        public static OptionResponse from(CustomizationOption o) {
            return new OptionResponse(o.getId(), o.getPart().name(), o.getName(), o.getColorHex(), o.getPriceDelta());
        }
    }

    /** Options grouped by part: CASE, DIAL, STRAP, HANDS. */
    public record CustomizationCatalog(Map<String, List<OptionResponse>> parts) {
    }
}
