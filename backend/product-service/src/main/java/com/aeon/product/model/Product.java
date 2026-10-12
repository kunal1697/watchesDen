package com.aeon.product.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "collection_name", nullable = false)
    private String collection;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "case_material", nullable = false)
    private String caseMaterial;

    @Column(nullable = false)
    private String movement;

    @Column(name = "diameter_mm", nullable = false)
    private int diameterMm;

    @Column(name = "water_resistance_m", nullable = false)
    private int waterResistanceM;

    /** Hex colours used by the frontend to render the watch as live SVG. */
    @Column(name = "case_color", nullable = false)
    private String caseColor;

    @Column(name = "dial_color", nullable = false)
    private String dialColor;

    @Column(name = "strap_color", nullable = false)
    private String strapColor;

    @Column(name = "accent_color", nullable = false)
    private String accentColor;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "hover_image_url")
    private String hoverImageUrl;

    @Column(nullable = false)
    private boolean customizable;

    @Column(nullable = false)
    private boolean active = true;

    /** Optimistic locking for admin edits racing each other. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCaseMaterial() { return caseMaterial; }
    public void setCaseMaterial(String caseMaterial) { this.caseMaterial = caseMaterial; }
    public String getMovement() { return movement; }
    public void setMovement(String movement) { this.movement = movement; }
    public int getDiameterMm() { return diameterMm; }
    public void setDiameterMm(int diameterMm) { this.diameterMm = diameterMm; }
    public int getWaterResistanceM() { return waterResistanceM; }
    public void setWaterResistanceM(int waterResistanceM) { this.waterResistanceM = waterResistanceM; }
    public String getCaseColor() { return caseColor; }
    public void setCaseColor(String caseColor) { this.caseColor = caseColor; }
    public String getDialColor() { return dialColor; }
    public void setDialColor(String dialColor) { this.dialColor = dialColor; }
    public String getStrapColor() { return strapColor; }
    public void setStrapColor(String strapColor) { this.strapColor = strapColor; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getHoverImageUrl() { return hoverImageUrl; }
    public void setHoverImageUrl(String hoverImageUrl) { this.hoverImageUrl = hoverImageUrl; }
    public boolean isCustomizable() { return customizable; }
    public void setCustomizable(boolean customizable) { this.customizable = customizable; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
