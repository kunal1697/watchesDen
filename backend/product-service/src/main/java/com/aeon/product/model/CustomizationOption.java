package com.aeon.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * One selectable part in the watch configurator, e.g. "Titanium case" or "Aurora dial".
 */
@Entity
@Table(name = "customization_options")
public class CustomizationOption {

    public enum Part { CASE, DIAL, STRAP, HANDS }

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Part part;

    @Column(nullable = false)
    private String name;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    @Column(name = "price_delta", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceDelta;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected CustomizationOption() {
    }

    public CustomizationOption(String id, Part part, String name, String colorHex, BigDecimal priceDelta, int sortOrder) {
        this.id = id;
        this.part = part;
        this.name = name;
        this.colorHex = colorHex;
        this.priceDelta = priceDelta;
        this.sortOrder = sortOrder;
    }

    public String getId() { return id; }
    public Part getPart() { return part; }
    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public BigDecimal getPriceDelta() { return priceDelta; }
    public int getSortOrder() { return sortOrder; }
}
