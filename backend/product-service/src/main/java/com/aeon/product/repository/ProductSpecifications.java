package com.aeon.product.repository;

import com.aeon.product.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Composable filters for the catalog search endpoint. A null argument means "no filter".
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> activeOnly() {
        return (root, q, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> matchesText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String like = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("collection")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("caseMaterial")), like),
                cb.like(cb.lower(root.get("movement")), like));
    }

    public static Specification<Product> inCollection(String collection) {
        if (collection == null || collection.isBlank()) {
            return null;
        }
        return (root, q, cb) -> cb.equal(cb.lower(root.get("collection")), collection.toLowerCase(Locale.ROOT));
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            }
            if (max != null) {
                return cb.lessThanOrEqualTo(root.get("price"), max);
            }
            return null;
        };
    }

    public static Specification<Product> customizable(Boolean customizable) {
        if (customizable == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("customizable"), customizable);
    }
}
