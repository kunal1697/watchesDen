package com.aeon.product.controller;

import com.aeon.common.dto.PageResponse;
import com.aeon.common.security.AuthHeaders;
import com.aeon.product.dto.ProductDtos.ProductRequest;
import com.aeon.product.dto.ProductDtos.ProductResponse;
import com.aeon.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Products", description = "Add, edit and delete products (admin only)")
@SecurityRequirement(name = "bearer")
public class AdminProductController {

    private final ProductService service;

    public AdminProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all products, including deactivated ones")
    public PageResponse<ProductResponse> list(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                              @ParameterObject @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        AuthHeaders.requireAdmin(role);
        return service.adminList(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public ProductResponse create(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                  @Valid @RequestBody ProductRequest request) {
        AuthHeaders.requireAdmin(role);
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse update(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                  @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        AuthHeaders.requireAdmin(role);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate (soft-delete) a product")
    public void delete(@Parameter(hidden = true) @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                       @PathVariable Long id) {
        AuthHeaders.requireAdmin(role);
        service.delete(id);
    }
}
