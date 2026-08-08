package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.ApiResponse;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart Operations", description = "Add, update, remove items, and view active shopping cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get user shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cart = cartService.getCartForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @Valid @RequestBody CartItemRequest cartItemRequest) {
        CartResponse cart = cartService.addItemToCart(userDetails.getUsername(), cartItemRequest);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @PathVariable Long itemId,
                                                                    @Valid @RequestBody UpdateCartItemRequest updateRequest) {
        CartResponse cart = cartService.updateCartItem(userDetails.getUsername(), itemId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @PathVariable Long itemId) {
        CartResponse cart = cartService.removeItemFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear shopping cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Shopping cart cleared"));
    }
}
