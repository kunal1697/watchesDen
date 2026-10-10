package com.aeon.order.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * order-service's view of product-service's internal inventory API.
 */
public interface ProductClient {

    record LineRequest(Long productId, int quantity, List<String> optionIds) {
    }

    record PricedLine(Long productId, String productName, int quantity, BigDecimal unitPrice,
                      List<String> optionIds, String customizationSummary, boolean available) {
    }

    record ReleaseLine(Long productId, int quantity) {
    }

    /** Prices lines without reserving stock; result order matches request order. */
    List<PricedLine> quote(List<LineRequest> lines);

    /** Reserves stock for every line or throws; nothing is reserved on failure. */
    List<PricedLine> reserve(List<LineRequest> lines);

    void release(List<ReleaseLine> lines);
}
