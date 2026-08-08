package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.api.dto.response.ApiResponse;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Order Management (Admin)", description = "Admin order management and status updates")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all customer orders (Admin)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<OrderResponse> orders = orderService.getAllOrdersAdmin(page, size);
        return ResponseEntity.ok(ApiResponse.success("All customer orders retrieved", orders));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (Admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest statusRequest) {
        OrderResponse order = orderService.updateOrderStatusAdmin(id, statusRequest);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
