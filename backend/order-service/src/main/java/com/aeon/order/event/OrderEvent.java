package com.aeon.order.event;

import com.aeon.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain event raised on every order lifecycle change. Published in-process today; the listener is
 * the seam where a Kafka/RabbitMQ producer plugs in (see ROADMAP.md, phase 2).
 */
public record OrderEvent(Type type, Long orderId, String orderNumber, Long userId, OrderStatus status,
                         BigDecimal totalAmount, Instant occurredAt) {

    public enum Type { ORDER_PLACED, ORDER_STATUS_CHANGED, ORDER_CANCELLED }
}
