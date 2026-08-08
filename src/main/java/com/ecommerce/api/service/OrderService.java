package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.OrderRequest;
import com.ecommerce.api.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.PagedResponse;

public interface OrderService {
    OrderResponse createOrder(String userEmail, OrderRequest orderRequest);
    OrderResponse getOrderById(String userEmail, Long orderId);
    PagedResponse<OrderResponse> getUserOrders(String userEmail, int page, int size);
    PagedResponse<OrderResponse> getAllOrdersAdmin(int page, int size);
    OrderResponse updateOrderStatusAdmin(Long orderId, OrderStatusUpdateRequest statusRequest);
}
