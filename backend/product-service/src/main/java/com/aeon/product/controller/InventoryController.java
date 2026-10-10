package com.aeon.product.controller;

import com.aeon.product.dto.InventoryDtos.QuoteResponse;
import com.aeon.product.dto.InventoryDtos.ReleaseRequest;
import com.aeon.product.dto.InventoryDtos.ReserveRequest;
import com.aeon.product.dto.InventoryDtos.ReserveResponse;
import com.aeon.product.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service API used by order-service. The gateway has no route to /internal/**,
 * so it is only reachable on the private Docker network.
 */
@RestController
@RequestMapping("/internal/inventory")
@Tag(name = "Internal · Inventory", description = "Service-to-service only; not exposed by the gateway")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/quote")
    @Operation(summary = "Price lines (base + customization) without reserving stock")
    public QuoteResponse quote(@Valid @RequestBody ReserveRequest request) {
        return new QuoteResponse(service.quote(request.items()));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Atomically reserve stock for all lines, or none")
    public ReserveResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return service.reserve(request);
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Return previously reserved stock (compensation)")
    public void release(@Valid @RequestBody ReleaseRequest request) {
        service.release(request);
    }
}
