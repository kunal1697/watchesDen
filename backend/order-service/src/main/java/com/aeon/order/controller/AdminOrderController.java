package com.aeon.order.controller;

import com.aeon.common.dto.PageResponse;
import com.aeon.common.security.AuthHeaders;
import com.aeon.order.dto.OrderDtos.OrderResponse;
import com.aeon.order.dto.OrderDtos.StatusUpdateRequest;
import com.aeon.order.model.OrderStatus;
import com.aeon.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin · Orders", description = "Order fulfilment (ADMIN role)")
@SecurityRequirement(name = "bearer")
public class AdminOrderController {

    private final OrderService service;

    public AdminOrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "All orders, optionally filtered by status")
    public PageResponse<OrderResponse> list(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                            @RequestParam(required = false) OrderStatus status,
                                            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        AuthHeaders.requireAdmin(role);
        return service.adminList(status, pageable);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Advance order status (PLACED → CONFIRMED → SHIPPED → DELIVERED, or CANCELLED)")
    public OrderResponse updateStatus(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                      @PathVariable Long orderId, @Valid @RequestBody StatusUpdateRequest request) {
        AuthHeaders.requireAdmin(role);
        return service.adminUpdateStatus(orderId, request.status());
    }
}
