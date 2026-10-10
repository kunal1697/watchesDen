package com.aeon.product.service;

import com.aeon.common.dto.PageResponse;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.product.dto.ProductDtos.CustomizationCatalog;
import com.aeon.product.dto.ProductDtos.OptionResponse;
import com.aeon.product.dto.ProductDtos.ProductRequest;
import com.aeon.product.dto.ProductDtos.ProductResponse;
import com.aeon.product.model.Product;
import com.aeon.product.repository.CustomizationOptionRepository;
import com.aeon.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.aeon.product.repository.ProductSpecifications.activeOnly;
import static com.aeon.product.repository.ProductSpecifications.customizable;
import static com.aeon.product.repository.ProductSpecifications.inCollection;
import static com.aeon.product.repository.ProductSpecifications.matchesText;
import static com.aeon.product.repository.ProductSpecifications.priceBetween;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository products;
    private final CustomizationOptionRepository options;

    public ProductService(ProductRepository products, CustomizationOptionRepository options) {
        this.products = products;
        this.options = options;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, String collection, BigDecimal minPrice, BigDecimal maxPrice,
                                                Boolean onlyCustomizable, Pageable pageable) {
        Specification<Product> spec = Specification.where(activeOnly())
                .and(matchesText(q))
                .and(inCollection(collection))
                .and(priceBetween(minPrice, maxPrice))
                .and(customizable(onlyCustomizable));
        return PageResponse.of(products.findAll(spec, pageable), ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return products.findByIdAndActiveTrue(id).map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public List<String> collections() {
        return products.findActiveCollections();
    }

    @Transactional(readOnly = true)
    public CustomizationCatalog customizations() {
        Map<String, List<OptionResponse>> grouped = new LinkedHashMap<>();
        options.findAllByOrderByPartAscSortOrderAsc().forEach(o ->
                grouped.computeIfAbsent(o.getPart().name(), k -> new java.util.ArrayList<>()).add(OptionResponse.from(o)));
        return new CustomizationCatalog(grouped);
    }

    // ---- Admin ----

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> adminList(Pageable pageable) {
        return PageResponse.of(products.findAll(pageable), ProductResponse::from);
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        Product saved = products.save(p);
        log.info("Admin created product id={} name='{}'", saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
        apply(p, req);
        log.info("Admin updated product id={}", id);
        return ProductResponse.from(products.saveAndFlush(p));
    }

    /** Soft delete: keeps historic orders' references valid, hides the product from the catalog. */
    @Transactional
    public void delete(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
        p.setActive(false);
        log.info("Admin deactivated product id={}", id);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setCollection(r.collection().trim());
        p.setDescription(r.description().trim());
        p.setPrice(r.price());
        p.setStock(r.stock());
        p.setCaseMaterial(r.caseMaterial().trim());
        p.setMovement(r.movement().trim());
        p.setDiameterMm(r.diameterMm());
        p.setWaterResistanceM(r.waterResistanceM());
        p.setCaseColor(r.caseColor().toUpperCase());
        p.setDialColor(r.dialColor().toUpperCase());
        p.setStrapColor(r.strapColor().toUpperCase());
        p.setAccentColor(r.accentColor().toUpperCase());
        p.setImageUrl(blankToNull(r.imageUrl()));
        p.setHoverImageUrl(blankToNull(r.hoverImageUrl()));
        p.setCustomizable(r.customizable());
        p.setActive(true);
    }
}
