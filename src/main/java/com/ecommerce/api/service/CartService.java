package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.CartResponse;

public interface CartService {
    CartResponse getCartForUser(String userEmail);
    CartResponse addItemToCart(String userEmail, CartItemRequest itemRequest);
    CartResponse updateCartItem(String userEmail, Long cartItemId, UpdateCartItemRequest updateRequest);
    CartResponse removeItemFromCart(String userEmail, Long cartItemId);
    void clearCart(String userEmail);
}
