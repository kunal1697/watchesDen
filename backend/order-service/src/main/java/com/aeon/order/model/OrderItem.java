package com.aeon.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.List;

/**
 * Snapshot of a purchased line: name and price are copied at checkout so later catalog
 * edits never change historic orders.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Convert(converter = OptionIdsConverter.class)
    @Column(name = "option_ids", nullable = false)
    private List<String> optionIds;

    @Column(name = "customization_summary")
    private String customizationSummary;

    protected OrderItem() {
    }

    public OrderItem(Long productId, String productName, BigDecimal unitPrice, int quantity,
                     List<String> optionIds, String customizationSummary) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.optionIds = optionIds == null ? List.of() : optionIds;
        this.customizationSummary = customizationSummary;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    void setOrder(Order order) { this.order = order; }
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public List<String> getOptionIds() { return optionIds; }
    public String getCustomizationSummary() { return customizationSummary; }
}
