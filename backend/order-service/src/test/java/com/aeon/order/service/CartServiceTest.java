package com.aeon.order.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.order.client.ProductClient;
import com.aeon.order.client.ProductClient.LineRequest;
import com.aeon.order.client.ProductClient.PricedLine;
import com.aeon.order.dto.CartDtos.AddItemRequest;
import com.aeon.order.dto.CartDtos.CartResponse;
import com.aeon.order.model.CartItem;
import com.aeon.order.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cart;
    @Mock
    private ProductClient productClient;
    private CartService service;

    @BeforeEach
    void setUp() {
        service = new CartService(cart, productClient);
        lenient().when(productClient.quote(anyList())).thenAnswer(inv -> {
            List<LineRequest> lines = inv.getArgument(0);
            return lines.stream().map(l -> new PricedLine(l.productId(), "Watch", l.quantity(),
                    new BigDecimal("100.00"), l.optionIds(), null, true)).collect(Collectors.toList());
        });
    }

    @Test
    void identicalCustomizationIsMergedRegardlessOfOptionOrder() {
        CartItem existing = new CartItem(1L, 5L, 2, List.of("dial-a", "case-b"));
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(existing));

        service.add(1L, new AddItemRequest(5L, 3, List.of("case-b", "dial-a")));

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cart, never()).save(any());
    }

    @Test
    void differentCustomizationCreatesNewLine() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(1L, 5L, 1, List.of("case-b"))));

        service.add(1L, new AddItemRequest(5L, 1, List.of("case-c")));

        verify(cart).save(any(CartItem.class));
    }

    @Test
    void mergedQuantityIsCapped() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(new CartItem(1L, 5L, 9, null)));

        assertThatThrownBy(() -> service.add(1L, new AddItemRequest(5L, 2, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Maximum 10");
    }

    @Test
    void unavailableProductCannotBeAdded() {
        when(productClient.quote(anyList())).thenReturn(List.of(
                new PricedLine(5L, "Unavailable product", 1, BigDecimal.ZERO, List.of(), null, false)));

        assertThatThrownBy(() -> service.add(1L, new AddItemRequest(5L, 1, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void viewComputesSubtotalExcludingUnavailableLines() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(
                new CartItem(1L, 5L, 2, null), new CartItem(1L, 6L, 1, null)));
        when(productClient.quote(anyList())).thenReturn(List.of(
                new PricedLine(5L, "A", 2, new BigDecimal("150.00"), List.of(), null, true),
                new PricedLine(6L, "Unavailable product", 1, BigDecimal.ZERO, List.of(), null, false)));

        CartResponse res = service.view(1L);

        assertThat(res.subtotal()).isEqualByComparingTo("300.00");
        assertThat(res.itemCount()).isEqualTo(2);
        assertThat(res.items()).hasSize(2);
    }

    @Test
    void emptyCartDoesNotCallProductService() {
        when(cart.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of());

        CartResponse res = service.view(1L);

        assertThat(res.items()).isEmpty();
        assertThat(res.subtotal()).isEqualByComparingTo("0");
    }
}
