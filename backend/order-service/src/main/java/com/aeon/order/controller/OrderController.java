package com.aeon.order.controller;

import com.aeon.common.dto.PageResponse;
import com.aeon.common.security.AuthHeaders;
import com.aeon.order.dto.OrderDtos.CheckoutRequest;
import com.aeon.order.dto.OrderDtos.OrderResponse;
import com.aeon.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Checkout and order history of the authenticated user")
@SecurityRequirement(name = "bearer")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order from the current cart",
            description = "Reserves stock, snapshots prices, clears the cart. Send an `Idempotency-Key` header to make retries safe.")
    public OrderResponse checkout(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                                  @Parameter(description = "Client-generated UUID; repeated keys return the original order")
                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                  @Valid @RequestBody CheckoutRequest request) {
        return service.checkout(AuthHeaders.requireUser(userId), request, idempotencyKey);
    }

    @GetMapping
    @Operation(summary = "Order history, newest first")
    public PageResponse<OrderResponse> history(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                                               @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.history(AuthHeaders.requireUser(userId), pageable);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "One order (only the owner can see it)")
    public OrderResponse get(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                             @PathVariable Long orderId) {
        return service.get(AuthHeaders.requireUser(userId), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order that has not shipped; stock is returned")
    public OrderResponse cancel(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ID, required = false) String userId,
                                @PathVariable Long orderId) {
        return service.cancel(AuthHeaders.requireUser(userId), orderId);
    }
}
