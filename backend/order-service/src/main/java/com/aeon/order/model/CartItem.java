package com.aeon.order.model;
// key mapping for optionIds list to a single string in the database
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Convert(converter = OptionIdsConverter.class)
    @Column(name = "option_ids", nullable = false)
    private List<String> optionIds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CartItem() {
    }

    public CartItem(Long userId, Long productId, int quantity, List<String> optionIds) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.optionIds = OptionIdsConverter.key(optionIds).isEmpty()
                ? List.of() : List.of(OptionIdsConverter.key(optionIds).split(","));
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public List<String> getOptionIds() { return optionIds; }
    public Instant getCreatedAt() { return createdAt; }
}
