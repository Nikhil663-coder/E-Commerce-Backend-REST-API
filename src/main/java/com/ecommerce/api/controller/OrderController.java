package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.OrderRequest;
import com.ecommerce.api.dto.response.ApiResponse;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "Checkout cart, place orders, and view order history")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place order / Checkout cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse order = orderService.createOrder(userDetails.getUsername(), orderRequest);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", order), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    @GetMapping
    @Operation(summary = "Get user order history with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<OrderResponse> orders = orderService.getUserOrders(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved", orders));
    }
}
