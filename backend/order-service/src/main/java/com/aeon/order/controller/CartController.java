package com.aeon.order.controller;

import com.aeon.common.security.AuthHeaders;
import com.aeon.order.dto.CartDtos.AddItemRequest;
import com.aeon.order.dto.CartDtos.CartResponse;
import com.aeon.order.dto.CartDtos.UpdateQuantityRequest;
import com.aeon.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart of the authenticated user")
@SecurityRequirement(name = "bearer")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "View cart with live prices")
    public CartResponse view(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId) {
        return service.view(AuthHeaders.requireUser(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a (optionally customized) watch; identical lines are merged")
    public CartResponse add(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                            @Valid @RequestBody AddItemRequest request) {
        return service.add(AuthHeaders.requireUser(userId), request);
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Change quantity of a cart line")
    public CartResponse update(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                               @PathVariable Long itemId, @Valid @RequestBody UpdateQuantityRequest request) {
        return service.updateQuantity(AuthHeaders.requireUser(userId), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove a cart line")
    public CartResponse remove(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                               @PathVariable Long itemId) {
        return service.remove(AuthHeaders.requireUser(userId), itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Empty the cart")
    public void clear(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId) {
        service.clear(AuthHeaders.requireUser(userId));
    }
}
