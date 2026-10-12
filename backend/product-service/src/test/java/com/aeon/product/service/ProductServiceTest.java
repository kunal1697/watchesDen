package com.aeon.product.service;

import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.product.dto.ProductDtos;
import com.aeon.product.dto.ProductDtos.ProductRequest;
import com.aeon.product.dto.ProductDtos.ProductResponse;
import com.aeon.product.model.CustomizationOption;
import com.aeon.product.model.CustomizationOption.Part;
import com.aeon.product.model.Product;
import com.aeon.product.repository.CustomizationOptionRepository;
import com.aeon.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository products;
    @Mock
    private CustomizationOptionRepository options;
    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(products, options);
    }

    @Test
    void createTrimsFieldsAndUppercasesColours() {
        when(products.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse res = service.create(request("  Orbit  "));

        assertThat(res.name()).isEqualTo("Orbit");
        assertThat(res.caseColor()).isEqualTo("#ABCDEF");
        assertThat(res.active()).isTrue();
        assertThat(res.inStock()).isTrue();
        assertThat(res.imageUrl()).isEqualTo("https://img.example/a.jpg");
        assertThat(res.hoverImageUrl()).isNull();
    }

    @Test
    void getMissingProductIs404() {
        when(products.findByIdAndActiveTrue(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteIsSoft() {
        Product p = new Product();
        p.setActive(true);
        when(products.findById(3L)).thenReturn(Optional.of(p));

        service.delete(3L);

        assertThat(p.isActive()).isFalse();
    }

    @Test
    void updateMissingProductIs404() {
        when(products.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(3L, request("X"))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void customizationsAreGroupedByPart() {
        when(options.findAllByOrderByPartAscSortOrderAsc()).thenReturn(List.of(
                new CustomizationOption("c1", Part.CASE, "Steel", "#AAAAAA", BigDecimal.ZERO, 1),
                new CustomizationOption("c2", Part.CASE, "Ti", "#BBBBBB", BigDecimal.TEN, 2),
                new CustomizationOption("d1", Part.DIAL, "Void", "#000000", BigDecimal.ZERO, 1)));

        ProductDtos.CustomizationCatalog catalog = service.customizations();

        assertThat(catalog.parts()).containsOnlyKeys("CASE", "DIAL");
        assertThat(catalog.parts().get("CASE")).hasSize(2);
    }

    private static ProductRequest request(String name) {
        return new ProductRequest(name, "Orbit", "desc", new BigDecimal("500.00"), 5, "Steel", "Automatic",
                40, 100, "#abcdef", "#000000", "#111111", "#00ffff", "https://img.example/a.jpg", " ", true);
    }
}
