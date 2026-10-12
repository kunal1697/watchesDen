package com.aeon.order.event;

import com.aeon.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public final class OrderEvent {

    public enum Type { ORDER_PLACED, ORDER_STATUS_CHANGED, ORDER_CANCELLED }

    private final Type type;
    private final Long orderId;
    private final String orderNumber;
    private final Long userId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final Instant occurredAt;

    public OrderEvent(Type type, Long orderId, String orderNumber, Long userId, OrderStatus status,
                      BigDecimal totalAmount, Instant occurredAt) {
        this.type = type;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.occurredAt = occurredAt;
    }

    public Type type() { return type; }
    public Long orderId() { return orderId; }
    public String orderNumber() { return orderNumber; }
    public Long userId() { return userId; }
    public OrderStatus status() { return status; }
    public BigDecimal totalAmount() { return totalAmount; }
    public Instant occurredAt() { return occurredAt; }
}
