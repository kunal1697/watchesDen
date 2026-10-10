package com.aeon.order.service;

import com.aeon.common.exception.BusinessException;
import com.aeon.common.exception.ResourceNotFoundException;
import com.aeon.order.client.ProductClient;
import com.aeon.order.client.ProductClient.LineRequest;
import com.aeon.order.client.ProductClient.PricedLine;
import com.aeon.order.dto.CartDtos.AddItemRequest;
import com.aeon.order.dto.CartDtos.CartLine;
import com.aeon.order.dto.CartDtos.CartResponse;
import com.aeon.order.model.CartItem;
import com.aeon.order.model.OptionIdsConverter;
import com.aeon.order.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    static final int MAX_QTY_PER_LINE = 10;
    static final int MAX_LINES = 20;
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartItemRepository cart;
    private final ProductClient productClient;

    public CartService(CartItemRepository cart, ProductClient productClient) {
        this.cart = cart;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public CartResponse view(Long userId) {
        return price(cart.findByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public CartResponse add(Long userId, AddItemRequest req) {
        // Validates product existence, customizability and option ids before persisting anything.
        PricedLine priced = productClient.quote(List.of(new LineRequest(req.productId(), req.quantity(),
                req.optionIds() == null ? List.of() : req.optionIds()))).get(0);
        if (!priced.available()) {
            throw new ResourceNotFoundException("Product", req.productId());
        }

        List<CartItem> items = cart.findByUserIdOrderByIdAsc(userId);
        String key = OptionIdsConverter.key(req.optionIds());
        CartItem existing = items.stream()
                .filter(i -> i.getProductId().equals(req.productId())
                        && OptionIdsConverter.key(i.getOptionIds()).equals(key))
                .findFirst().orElse(null);

        if (existing != null) {
            int qty = existing.getQuantity() + req.quantity();
            if (qty > MAX_QTY_PER_LINE) {
                throw new BusinessException(400, "Maximum " + MAX_QTY_PER_LINE + " units per item");
            }
            existing.setQuantity(qty);
        } else {
            if (items.size() >= MAX_LINES) {
                throw new BusinessException(400, "Cart is full (max " + MAX_LINES + " items)");
            }
            cart.save(new CartItem(userId, req.productId(), req.quantity(), req.optionIds()));
        }
        log.info("User {} added product {} x{} to cart", userId, req.productId(), req.quantity());
        return price(cart.findByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long itemId, int quantity) {
        CartItem item = cart.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        item.setQuantity(quantity);
        return price(cart.findByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public CartResponse remove(Long userId, Long itemId) {
        CartItem item = cart.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        cart.delete(item);
        return price(cart.findByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public void clear(Long userId) {
        cart.deleteAllForUser(userId);
    }

    private CartResponse price(List<CartItem> items) {
        if (items.isEmpty()) {
            return new CartResponse(List.of(), 0, BigDecimal.ZERO);
        }
        List<PricedLine> priced = productClient.quote(items.stream()
                .map(i -> new LineRequest(i.getProductId(), i.getQuantity(), i.getOptionIds())).toList());
        List<CartLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            PricedLine p = priced.get(i);
            BigDecimal lineTotal = p.unitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            lines.add(new CartLine(item.getId(), item.getProductId(), p.productName(), item.getQuantity(),
                    p.unitPrice(), lineTotal, item.getOptionIds(), p.customizationSummary(), p.available()));
            if (p.available()) {
                subtotal = subtotal.add(lineTotal);
                count += item.getQuantity();
            }
        }
        return new CartResponse(lines, count, subtotal);
    }
}
