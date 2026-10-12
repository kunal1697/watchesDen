package com.aeon.product.dto;

import com.aeon.product.model.CustomizationOption;
import com.aeon.product.model.Product;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ProductRequest {
        @Schema(example = "Nebula Chrono X1") @NotBlank @Size(max = 120)
        private final String name;
        @Schema(example = "Nebula") @NotBlank @Size(max = 60)
        private final String collection;
        @Schema(example = "A chronograph forged for deep-space timekeeping.") @NotBlank @Size(max = 2000)
        private final String description;
        @Schema(example = "1299.00") @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        private final BigDecimal price;
        @Schema(example = "25") @Min(0) @Max(100000)
        private final int stock;
        @Schema(example = "Grade 5 Titanium") @NotBlank @Size(max = 60)
        private final String caseMaterial;
        @Schema(example = "Automatic") @NotBlank @Size(max = 60)
        private final String movement;
        @Schema(example = "42") @Min(20) @Max(60)
        private final int diameterMm;
        @Schema(example = "100") @Min(0) @Max(1000)
        private final int waterResistanceM;
        @Schema(example = "#C0C6CF") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG)
        private final String caseColor;
        @Schema(example = "#0B1026") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG)
        private final String dialColor;
        @Schema(example = "#1C1F2A") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG)
        private final String strapColor;
        @Schema(example = "#00F0FF") @NotBlank @Pattern(regexp = HEX, message = HEX_MSG)
        private final String accentColor;
        @Schema(example = "https://images.unsplash.com/photo-1523170335258-f5ed11844a49")
        @Size(max = 500) @Pattern(regexp = URL, message = URL_MSG)
        private final String imageUrl;
        @Schema(example = "https://images.unsplash.com/photo-1727716919539-3cd4aaf7af99")
        @Size(max = 500) @Pattern(regexp = URL, message = URL_MSG)
        private final String hoverImageUrl;
        private final boolean customizable;

        @JsonCreator
        public ProductRequest(@JsonProperty("name") String name,
                              @JsonProperty("collection") String collection,
                              @JsonProperty("description") String description,
                              @JsonProperty("price") BigDecimal price,
                              @JsonProperty("stock") int stock,
                              @JsonProperty("caseMaterial") String caseMaterial,
                              @JsonProperty("movement") String movement,
                              @JsonProperty("diameterMm") int diameterMm,
                              @JsonProperty("waterResistanceM") int waterResistanceM,
                              @JsonProperty("caseColor") String caseColor,
                              @JsonProperty("dialColor") String dialColor,
                              @JsonProperty("strapColor") String strapColor,
                              @JsonProperty("accentColor") String accentColor,
                              @JsonProperty("imageUrl") String imageUrl,
                              @JsonProperty("hoverImageUrl") String hoverImageUrl,
                              @JsonProperty("customizable") boolean customizable) {
            this.name = name;
            this.collection = collection;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.caseMaterial = caseMaterial;
            this.movement = movement;
            this.diameterMm = diameterMm;
            this.waterResistanceM = waterResistanceM;
            this.caseColor = caseColor;
            this.dialColor = dialColor;
            this.strapColor = strapColor;
            this.accentColor = accentColor;
            this.imageUrl = imageUrl;
            this.hoverImageUrl = hoverImageUrl;
            this.customizable = customizable;
        }

        public String name() { return name; }
        public String collection() { return collection; }
        public String description() { return description; }
        public BigDecimal price() { return price; }
        public int stock() { return stock; }
        public String caseMaterial() { return caseMaterial; }
        public String movement() { return movement; }
        public int diameterMm() { return diameterMm; }
        public int waterResistanceM() { return waterResistanceM; }
        public String caseColor() { return caseColor; }
        public String dialColor() { return dialColor; }
        public String strapColor() { return strapColor; }
        public String accentColor() { return accentColor; }
        public String imageUrl() { return imageUrl; }
        public String hoverImageUrl() { return hoverImageUrl; }
        public boolean customizable() { return customizable; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ProductResponse {
        private final Long id;
        private final String name;
        private final String collection;
        private final String description;
        private final BigDecimal price;
        private final int stock;
        private final boolean inStock;
        private final String caseMaterial;
        private final String movement;
        private final int diameterMm;
        private final int waterResistanceM;
        private final String caseColor;
        private final String dialColor;
        private final String strapColor;
        private final String accentColor;
        private final String imageUrl;
        private final String hoverImageUrl;
        private final boolean customizable;
        private final boolean active;
        private final Instant createdAt;
        private final Instant updatedAt;

        public ProductResponse(Long id, String name, String collection, String description, BigDecimal price,
                               int stock, boolean inStock, String caseMaterial, String movement, int diameterMm,
                               int waterResistanceM, String caseColor, String dialColor, String strapColor,
                               String accentColor, String imageUrl, String hoverImageUrl,
                               boolean customizable, boolean active,
                               Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.collection = collection;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.inStock = inStock;
            this.caseMaterial = caseMaterial;
            this.movement = movement;
            this.diameterMm = diameterMm;
            this.waterResistanceM = waterResistanceM;
            this.caseColor = caseColor;
            this.dialColor = dialColor;
            this.strapColor = strapColor;
            this.accentColor = accentColor;
            this.imageUrl = imageUrl;
            this.hoverImageUrl = hoverImageUrl;
            this.customizable = customizable;
            this.active = active;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getCollection(), p.getDescription(), p.getPrice(),
                    p.getStock(), p.getStock() > 0, p.getCaseMaterial(), p.getMovement(), p.getDiameterMm(),
                    p.getWaterResistanceM(), p.getCaseColor(), p.getDialColor(), p.getStrapColor(),
                    p.getAccentColor(), p.getImageUrl(), p.getHoverImageUrl(), p.isCustomizable(), p.isActive(), p.getCreatedAt(), p.getUpdatedAt());
        }

        public Long id() { return id; }
        public String name() { return name; }
        public String collection() { return collection; }
        public String description() { return description; }
        public BigDecimal price() { return price; }
        public int stock() { return stock; }
        public boolean inStock() { return inStock; }
        public String caseMaterial() { return caseMaterial; }
        public String movement() { return movement; }
        public int diameterMm() { return diameterMm; }
        public int waterResistanceM() { return waterResistanceM; }
        public String caseColor() { return caseColor; }
        public String dialColor() { return dialColor; }
        public String strapColor() { return strapColor; }
        public String accentColor() { return accentColor; }
        public String imageUrl() { return imageUrl; }
        public String hoverImageUrl() { return hoverImageUrl; }
        public boolean customizable() { return customizable; }
        public boolean active() { return active; }
        public Instant createdAt() { return createdAt; }
        public Instant updatedAt() { return updatedAt; }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class OptionResponse {
        private final String id;
        private final String part;
        private final String name;
        private final String colorHex;
        private final BigDecimal priceDelta;

        public OptionResponse(String id, String part, String name, String colorHex, BigDecimal priceDelta) {
            this.id = id;
            this.part = part;
            this.name = name;
            this.colorHex = colorHex;
            this.priceDelta = priceDelta;
        }

        public static OptionResponse from(CustomizationOption o) {
            return new OptionResponse(o.getId(), o.getPart().name(), o.getName(), o.getColorHex(), o.getPriceDelta());
        }

        public String id() { return id; }
        public String part() { return part; }
        public String name() { return name; }
        public String colorHex() { return colorHex; }
        public BigDecimal priceDelta() { return priceDelta; }
    }

    /** Options grouped by part: CASE, DIAL, STRAP, HANDS. */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class CustomizationCatalog {
        private final Map<String, List<OptionResponse>> parts;

        public CustomizationCatalog(Map<String, List<OptionResponse>> parts) {
            this.parts = parts;
        }

        public Map<String, List<OptionResponse>> parts() { return parts; }
    }
}
