package com.aeon.order.service;

import com.aeon.common.dto.PageResponse;
import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.order.client.ProductClient;
import com.aeon.order.client.ProductClient.LineRequest;
import com.aeon.order.client.ProductClient.PricedLine;
import com.aeon.order.client.ProductClient.ReleaseLine;
import com.aeon.order.dto.OrderDtos.CheckoutRequest;
import com.aeon.order.dto.OrderDtos.OrderResponse;
import com.aeon.order.dto.OrderDtos.ShippingAddress;
import com.aeon.order.event.OrderEvent;
import com.aeon.order.model.CartItem;
import com.aeon.order.model.Order;
import com.aeon.order.model.OrderItem;
import com.aeon.order.model.OrderStatus;
import com.aeon.order.repository.CartItemRepository;
import com.aeon.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final ProductClient productClient;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate tx;

    public OrderService(OrderRepository orders, CartItemRepository cart, ProductClient productClient,
                        ApplicationEventPublisher events, TransactionTemplate tx) {
        this.orders = orders;
        this.cart = cart;
        this.productClient = productClient;
        this.events = events;
        this.tx = tx;
    }

    public OrderResponse checkout(Long userId, CheckoutRequest req, String idempotencyKey) {
        String key = (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey.trim();
        if (key != null) {
            OrderResponse previous = existing(userId, key);
            if (previous != null) {
                log.info("Idempotent replay of checkout key={} -> order {}", key, previous.orderNumber());
                return previous;
            }
        }

        List<CartItem> items = cart.findByUserIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            throw new BusinessException(400, "Your cart is empty");
        }

        List<PricedLine> reserved = productClient.reserve(items.stream()
                .map(i -> new LineRequest(i.getProductId(), i.getQuantity(), i.getOptionIds())).collect(Collectors.toList()));

        try {
            OrderResponse placed = tx.execute(status -> {
                ShippingAddress s = req.shipping();
                Order order = new Order(newOrderNumber(), userId, key, req.paymentMethod(), s.fullName().trim(),
                        s.addressLine().trim(), s.city().trim(), s.postalCode().trim().toUpperCase(), s.country().trim());
                reserved.forEach(l -> order.addItem(new OrderItem(l.productId(), l.productName(), l.unitPrice(),
                        l.quantity(), l.optionIds(), l.customizationSummary())));
                Order saved = orders.saveAndFlush(order);
                cart.deleteAllForUser(userId);
                publish(OrderEvent.Type.ORDER_PLACED, saved);
                return OrderResponse.from(saved);
            });
            log.info("Order {} placed by user {} total={}", placed.orderNumber(), userId, placed.totalAmount());
            return placed;
        } catch (DataIntegrityViolationException e) {
            compensate(reserved);
            // same idempotency key was used by another request at the same time
            if (key != null) {
                OrderResponse winner = existing(userId, key);
                if (winner != null) {
                    return winner;
                }
            }
            throw e;
        } catch (RuntimeException e) {
            compensate(reserved);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> history(Long userId, Pageable pageable) {
        return PageResponse.of(orders.findByUserId(userId, pageable), OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long userId, Long orderId) {
        return OrderResponse.from(owned(userId, orderId));
    }

    public OrderResponse cancel(Long userId, Long orderId) {
        OrderResponse cancelled = transition(orderId, OrderStatus.CANCELLED, userId);
        compensate(cancelled.items().stream()
                .map(i -> new PricedLine(i.productId(), i.productName(), i.quantity(), i.unitPrice(),
                        i.optionIds(), i.customizationSummary(), true)).collect(Collectors.toList()));
        return cancelled;
    }

    // admin

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminList(OrderStatus status, Pageable pageable) {
        var page = status == null ? orders.findAll(pageable) : orders.findByStatus(status, pageable);
        return PageResponse.of(page, OrderResponse::from);
    }

    public OrderResponse adminUpdateStatus(Long orderId, OrderStatus next) {
        OrderResponse updated = transition(orderId, next, null);
        if (next == OrderStatus.CANCELLED) {
            compensate(updated.items().stream()
                    .map(i -> new PricedLine(i.productId(), i.productName(), i.quantity(), i.unitPrice(),
                            i.optionIds(), i.customizationSummary(), true)).collect(Collectors.toList()));
        }
        return updated;
    }

    private OrderResponse transition(Long orderId, OrderStatus next, Long ownerId) {
        try {
            return tx.execute(status -> {
                Order order = ownerId == null
                        ? orders.findWithItemsById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId))
                        : owned(ownerId, orderId);
                if (!order.getStatus().canTransitionTo(next)) {
                    throw new BusinessException("Cannot change order from " + order.getStatus() + " to " + next);
                }
                order.setStatus(next);
                orders.saveAndFlush(order);
                publish(next == OrderStatus.CANCELLED ? OrderEvent.Type.ORDER_CANCELLED
                        : OrderEvent.Type.ORDER_STATUS_CHANGED, order);
                log.info("Order {} -> {}", order.getOrderNumber(), next);
                return OrderResponse.from(order);
            });
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Order was modified concurrently, please retry");
        }
    }

    private Order owned(Long userId, Long orderId) {
        return orders.findWithItemsById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private OrderResponse existing(Long userId, String key) {
        return tx.execute(s -> orders.findByUserIdAndIdempotencyKey(userId, key).map(OrderResponse::from).orElse(null));
    }

    private void compensate(List<PricedLine> lines) {
        try {
            productClient.release(lines.stream().map(l -> new ReleaseLine(l.productId(), l.quantity())).collect(Collectors.toList()));
        } catch (RuntimeException e) {
            // TODO: retry later, for now just log it
            log.error("COMPENSATION FAILED - stock not released for {}: {}", lines, e.getMessage());
        }
    }

    private void publish(OrderEvent.Type type, Order o) {
        events.publishEvent(new OrderEvent(type, o.getId(), o.getOrderNumber(), o.getUserId(), o.getStatus(),
                o.getTotalAmount(), Instant.now()));
    }

    static String newOrderNumber() {
        byte[] b = new byte[3];
        RANDOM.nextBytes(b);
        return "WD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + String.format("%02X%02X%02X", b[0], b[1], b[2]);
    }
}
