package com.aeon.product.controller;

import com.aeon.common.dto.PageResponse;
import com.aeon.product.dto.ProductDtos.CustomizationCatalog;
import com.aeon.product.dto.ProductDtos.ProductResponse;
import com.aeon.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Catalog", description = "Public product listing, search, details and configurator options")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/api/products")
    @Operation(summary = "List & search watches",
            description = "Filter by free text, collection, price range and customizability. Sort e.g. `price,asc`.")
    public PageResponse<ProductResponse> search(
            @Parameter(description = "Free-text search over name, collection, description, material")
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String collection,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean customizable,
            @ParameterObject @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.search(q, collection, minPrice, maxPrice, customizable, pageable);
    }

    @GetMapping("/api/products/{id}")
    @Operation(summary = "Product details")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/api/products/collections")
    @Operation(summary = "Distinct collection names, for filter chips")
    public List<String> collections() {
        return service.collections();
    }

    @GetMapping("/api/customizations")
    @Operation(summary = "Configurator options grouped by part (CASE, DIAL, STRAP, HANDS)")
    public CustomizationCatalog customizations() {
        return service.customizations();
    }
}
