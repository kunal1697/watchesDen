package com.aeon.order.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.order.client.ProductClient;
import com.aeon.order.client.ProductClient.PricedLine;
import com.aeon.order.dto.OrderDtos.CheckoutRequest;
import com.aeon.order.dto.OrderDtos.OrderResponse;
import com.aeon.order.dto.OrderDtos.ShippingAddress;
import com.aeon.order.event.OrderEvent;
import com.aeon.order.model.CartItem;
import com.aeon.order.model.Order;
import com.aeon.order.model.OrderStatus;
import com.aeon.order.repository.CartItemRepository;
import com.aeon.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final CheckoutRequest CHECKOUT = new CheckoutRequest(
            new ShippingAddress("Ada", "1 Loop St", "London", "nw1 6xe", "UK"), "CARD");

    @Mock
    private OrderRepository orders;
    @Mock
    private CartItemRepository cart;
    @Mock
    private ProductClient productClient;
    @Mock
    private ApplicationEventPublisher events;
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, cart, productClient, events,
                new TransactionTemplate(mock(PlatformTransactionManager.class)));
    }

    @Test
    void checkoutWithEmptyCartIsRejected() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkout(1L, CHECKOUT, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Your cart is empty");
        verify(productClient, never()).reserve(anyList());
    }

    @Test
    void checkoutSnapshotsPricesClearsCartAndPublishesEvent() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(1L, 5L, 2, List.of("case-ti"))));
        when(productClient.reserve(anyList())).thenReturn(List.of(
                new PricedLine(5L, "Nebula", 2, new BigDecimal("1150.00"), List.of("case-ti"), "Titanium", true)));
        when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = service.checkout(1L, CHECKOUT, null);

        assertThat(res.totalAmount()).isEqualByComparingTo("2300.00");
        assertThat(res.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(res.orderNumber()).startsWith("WD-");
        assertThat(res.shipping().postalCode()).isEqualTo("NW1 6XE");
        assertThat(res.items()).singleElement().satisfies(i -> assertThat(i.customizationSummary()).isEqualTo("Titanium"));
        verify(cart).deleteAllForUser(1L);
        ArgumentCaptor<OrderEvent> event = ArgumentCaptor.forClass(OrderEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().type()).isEqualTo(OrderEvent.Type.ORDER_PLACED);
    }

    @Test
    void failedPersistenceReleasesReservedStock() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(1L, 5L, 2, null)));
        when(productClient.reserve(anyList())).thenReturn(List.of(
                new PricedLine(5L, "Nebula", 2, new BigDecimal("100.00"), List.of(), null, true)));
        when(orders.saveAndFlush(any(Order.class))).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.checkout(1L, CHECKOUT, null)).isInstanceOf(IllegalStateException.class);

        verify(productClient).release(List.of(new ProductClient.ReleaseLine(5L, 2)));
    }

    @Test
    void duplicateIdempotencyKeyReturnsOriginalOrderWithoutReserving() {
        Order previous = order(OrderStatus.PLACED);
        when(orders.findByUserIdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.of(previous));

        OrderResponse res = service.checkout(1L, CHECKOUT, "key-1");

        assertThat(res.orderNumber()).isEqualTo(previous.getOrderNumber());
        verify(productClient, never()).reserve(anyList());
    }

    @Test
    void racingDuplicateKeyCompensatesAndReturnsWinner() {
        Order winner = order(OrderStatus.PLACED);
        when(orders.findByUserIdAndIdempotencyKey(1L, "k")).thenReturn(Optional.empty(), Optional.of(winner));
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(1L, 5L, 1, null)));
        when(productClient.reserve(anyList())).thenReturn(List.of(
                new PricedLine(5L, "Nebula", 1, new BigDecimal("100.00"), List.of(), null, true)));
        when(orders.saveAndFlush(any(Order.class))).thenThrow(new DataIntegrityViolationException("dup"));

        OrderResponse res = service.checkout(1L, CHECKOUT, "k");

        assertThat(res.orderNumber()).isEqualTo(winner.getOrderNumber());
        verify(productClient).release(anyList());
    }

    @Test
    void cancelReleasesStock() {
        when(orders.findWithItemsById(9L)).thenReturn(Optional.of(order(OrderStatus.PLACED)));
        when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = service.cancel(1L, 9L);

        assertThat(res.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(productClient).release(List.of(new ProductClient.ReleaseLine(5L, 1)));
    }

    @Test
    void cannotCancelShippedOrder() {
        when(orders.findWithItemsById(9L)).thenReturn(Optional.of(order(OrderStatus.SHIPPED)));

        assertThatThrownBy(() -> service.cancel(1L, 9L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SHIPPED to CANCELLED");
        verify(productClient, never()).release(anyList());
    }

    @Test
    void otherUsersOrderIsNotFound() {
        when(orders.findWithItemsById(9L)).thenReturn(Optional.of(order(OrderStatus.PLACED)));

        assertThatThrownBy(() -> service.get(2L, 9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void statusMachine() {
        assertThat(OrderStatus.PLACED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PLACED)).isFalse();
    }

    private static Order order(OrderStatus status) {
        Order o = new Order("WD-20261009-ABCDEF", 1L, null, "CARD", "Ada", "1 Loop St", "London", "NW1", "UK");
        o.addItem(new com.aeon.order.model.OrderItem(5L, "Nebula", new BigDecimal("100.00"), 1, List.of(), null));
        o.setStatus(status);
        ReflectionTestUtils.setField(o, "id", 9L);
        return o;
    }
}
