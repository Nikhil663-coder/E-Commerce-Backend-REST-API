package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.dto.response.CartItemResponse;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.InsufficientStockException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public CartResponse getCartForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Cart cart = getOrCreateCart(user);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String userEmail, CartItemRequest itemRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not available for purchase");
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int targetQuantity = itemRequest.getQuantity();
        if (existingItem.isPresent()) {
            targetQuantity += existingItem.get().getQuantity();
        }

        if (product.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException(
                    String.format("Requested quantity (%d) exceeds available stock (%d) for product '%s'",
                            targetQuantity, product.getStockQuantity(), product.getTitle())
            );
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(targetQuantity);
            item.setUnitPrice(product.getPrice());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, itemRequest.getQuantity(), product.getPrice());
            cart.addItem(newItem);
            cartRepository.save(cart);
        }

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userEmail, Long cartItemId, UpdateCartItemRequest updateRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to user cart");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < updateRequest.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Requested quantity (%d) exceeds available stock (%d) for product '%s'",
                            updateRequest.getQuantity(), product.getStockQuantity(), product.getTitle())
            );
        }

        cartItem.setQuantity(updateRequest.getQuantity());
        cartItem.setUnitPrice(product.getPrice());
        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(String userEmail, Long cartItemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to user cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Cart cart = getOrCreateCart(user);
        cart.clearCart();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    Product p = item.getProduct();
                    CategoryResponse categoryResponse = new CategoryResponse(
                            p.getCategory().getId(),
                            p.getCategory().getName(),
                            p.getCategory().getDescription(),
                            p.getCategory().getSlug(),
                            p.getCategory().getCreatedAt()
                    );
                    ProductResponse productResponse = new ProductResponse(
                            p.getId(),
                            p.getTitle(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getStockQuantity(),
                            categoryResponse,
                            p.getImageUrl(),
                            p.getSku(),
                            p.getActive(),
                            p.getCreatedAt(),
                            p.getUpdatedAt()
                    );
                    return new CartItemResponse(
                            item.getId(),
                            productResponse,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()
                    );
                })
                .collect(Collectors.toList());

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), itemResponses, grandTotal);
    }
}
