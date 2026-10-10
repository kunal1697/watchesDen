package com.aeon.product.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.product.dto.InventoryDtos.ReleaseRequest;
import com.aeon.product.dto.InventoryDtos.ReserveItem;
import com.aeon.product.dto.InventoryDtos.ReserveRequest;
import com.aeon.product.dto.InventoryDtos.ReserveResponse;
import com.aeon.product.dto.InventoryDtos.ReservedLine;
import com.aeon.product.model.CustomizationOption;
import com.aeon.product.model.Product;
import com.aeon.product.repository.CustomizationOptionRepository;
import com.aeon.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Prices cart lines (base price + customization deltas) and reserves stock for checkout.
 * Reservation is all-or-nothing: one transaction, rolled back if any line lacks stock.
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository products;
    private final CustomizationOptionRepository options;

    public InventoryService(ProductRepository products, CustomizationOptionRepository options) {
        this.products = products;
        this.options = options;
    }

    /** Prices lines without touching stock. Lines for removed products come back as unavailable. */
    @Transactional(readOnly = true)
    public List<ReservedLine> quote(List<ReserveItem> items) {
        Map<String, CustomizationOption> optionIndex = optionIndex();
        return items.stream().map(i -> products.findByIdAndActiveTrue(i.productId())
                        .map(p -> price(i, p, optionIndex))
                        .orElseGet(() -> new ReservedLine(i.productId(), "Unavailable product", i.quantity(),
                                BigDecimal.ZERO, i.safeOptionIds(), null, false)))
                .toList();
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        Map<String, CustomizationOption> optionIndex = optionIndex();
        List<ReservedLine> lines = new ArrayList<>();
        // Lock rows in a consistent order to avoid deadlocks between concurrent checkouts.
        List<ReserveItem> ordered = request.items().stream()
                .sorted(Comparator.comparing(ReserveItem::productId)).toList();
        for (ReserveItem item : ordered) {
            Product p = loadActive(item.productId());
            ReservedLine line = price(item, p, optionIndex);
            if (products.decrementStock(p.getId(), item.quantity()) == 0) {
                log.warn("Insufficient stock for product id={} requested={}", p.getId(), item.quantity());
                throw new BusinessException("Insufficient stock for " + p.getName());
            }
            lines.add(line);
        }
        log.info("Reserved stock for {} line(s)", lines.size());
        return new ReserveResponse(lines);
    }

    /** Compensation: returns stock when an order is cancelled or a checkout fails after reservation. */
    @Transactional
    public void release(ReleaseRequest request) {
        request.items().forEach(i -> products.incrementStock(i.productId(), i.quantity()));
        log.info("Released stock for {} line(s)", request.items().size());
    }

    private Product loadActive(Long id) {
        return products.findByIdAndActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Map<String, CustomizationOption> optionIndex() {
        return options.findAll().stream().collect(Collectors.toMap(CustomizationOption::getId, Function.identity()));
    }

    static ReservedLine price(ReserveItem item, Product p, Map<String, CustomizationOption> optionIndex) {
        List<String> ids = item.safeOptionIds();
        if (!ids.isEmpty() && !p.isCustomizable()) {
            throw new BusinessException(400, p.getName() + " cannot be customized");
        }
        BigDecimal unit = p.getPrice();
        EnumSet<CustomizationOption.Part> seen = EnumSet.noneOf(CustomizationOption.Part.class);
        List<String> names = new ArrayList<>();
        for (String id : ids) {
            CustomizationOption o = optionIndex.get(id);
            if (o == null) {
                throw new BusinessException(400, "Unknown customization option: " + id);
            }
            if (!seen.add(o.getPart())) {
                throw new BusinessException(400, "Only one option allowed per part: " + o.getPart());
            }
            unit = unit.add(o.getPriceDelta());
            names.add(o.getName());
        }
        return new ReservedLine(p.getId(), p.getName(), item.quantity(), unit, ids,
                names.isEmpty() ? null : String.join(" · ", names), true);
    }
}
