package com.aeon.product.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.product.dto.InventoryDtos.ReleaseItem;
import com.aeon.product.dto.InventoryDtos.ReleaseRequest;
import com.aeon.product.dto.InventoryDtos.ReserveItem;
import com.aeon.product.dto.InventoryDtos.ReserveRequest;
import com.aeon.product.dto.InventoryDtos.ReserveResponse;
import com.aeon.product.dto.InventoryDtos.ReservedLine;
import com.aeon.product.model.CustomizationOption;
import com.aeon.product.model.CustomizationOption.Part;
import com.aeon.product.model.Product;
import com.aeon.product.repository.CustomizationOptionRepository;
import com.aeon.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository products;
    @Mock
    private CustomizationOptionRepository options;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(products, options);
        lenient().when(options.findAll()).thenReturn(List.of(
                new CustomizationOption("case-ti", Part.CASE, "Titanium", "#C0C6CF", new BigDecimal("150.00"), 1),
                new CustomizationOption("case-st", Part.CASE, "Steel", "#AEB6BF", BigDecimal.ZERO, 2),
                new CustomizationOption("dial-aurora", Part.DIAL, "Aurora", "#08332B", new BigDecimal("80.00"), 1)));
    }

    @Test
    void quoteAddsCustomizationDeltasToBasePrice() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "1000.00", true)));

        List<ReservedLine> lines = service.quote(List.of(new ReserveItem(1L, 2, List.of("case-ti", "dial-aurora"))));

        assertThat(lines).singleElement().satisfies(l -> {
            assertThat(l.unitPrice()).isEqualByComparingTo("1230.00");
            assertThat(l.quantity()).isEqualTo(2);
            assertThat(l.customizationSummary()).isEqualTo("Titanium · Aurora");
        });
    }

    @Test
    void quoteMarksRemovedProductsUnavailable() {
        when(products.findByIdAndActiveTrue(8L)).thenReturn(Optional.empty());

        List<ReservedLine> lines = service.quote(List.of(new ReserveItem(8L, 1, null)));

        assertThat(lines).singleElement().satisfies(l -> assertThat(l.available()).isFalse());
    }

    @Test
    void customizingNonCustomizableProductIsRejected() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "1000.00", false)));

        assertThatThrownBy(() -> service.quote(List.of(new ReserveItem(1L, 1, List.of("case-ti")))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be customized");
    }

    @Test
    void unknownOptionIsRejected() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "1000.00", true)));

        assertThatThrownBy(() -> service.quote(List.of(new ReserveItem(1L, 1, List.of("case-unobtainium")))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown customization option");
    }

    @Test
    void twoOptionsForSamePartAreRejected() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "1000.00", true)));

        assertThatThrownBy(() -> service.quote(List.of(new ReserveItem(1L, 1, List.of("case-ti", "case-st")))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only one option allowed per part");
    }

    @Test
    void reserveDecrementsStockInProductIdOrder() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "100.00", true)));
        when(products.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(product(2L, "200.00", true)));
        when(products.decrementStock(1L, 1)).thenReturn(1);
        when(products.decrementStock(2L, 3)).thenReturn(1);

        ReserveResponse res = service.reserve(new ReserveRequest(List.of(
                new ReserveItem(2L, 3, null), new ReserveItem(1L, 1, null))));

        assertThat(res.lines()).extracting(ReservedLine::productId).containsExactly(1L, 2L);
        InOrder order = inOrder(products);
        order.verify(products).decrementStock(1L, 1);
        order.verify(products).decrementStock(2L, 3);
    }

    @Test
    void reserveFailsWhenStockInsufficient() {
        when(products.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product(1L, "100.00", true)));
        when(products.decrementStock(1L, 5)).thenReturn(0);

        assertThatThrownBy(() -> service.reserve(new ReserveRequest(List.of(new ReserveItem(1L, 5, null)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void reserveOfMissingProductIs404() {
        when(products.findByIdAndActiveTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserve(new ReserveRequest(List.of(new ReserveItem(9L, 1, null)))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void releaseIncrementsStock() {
        service.release(new ReleaseRequest(List.of(new ReleaseItem(4L, 2))));

        verify(products).incrementStock(4L, 2);
    }

    private static Product product(Long id, String price, boolean customizable) {
        Product p = new Product();
        ReflectionTestUtils.setField(p, "id", id);
        p.setName("Watch " + id);
        p.setPrice(new BigDecimal(price));
        p.setCustomizable(customizable);
        p.setStock(10);
        return p;
    }
}
