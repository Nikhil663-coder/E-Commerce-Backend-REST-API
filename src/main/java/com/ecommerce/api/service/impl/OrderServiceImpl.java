package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.OrderRequest;
import com.ecommerce.api.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.api.dto.response.*;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.InsufficientStockException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.*;
import com.ecommerce.api.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String userEmail, OrderRequest orderRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Shopping cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty shopping cart");
        }

        // Validate stock levels before placing order
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getTitle(), product.getStockQuantity(), cartItem.getQuantity())
                );
            }
        }

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = new Order(
                orderNumber,
                user,
                totalAmount,
                OrderStatus.PENDING,
                orderRequest.getShippingAddress(),
                "PAID" // Simulated payment completion
        );

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Deduct stock quantity
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice(),
                    subtotal
            );
            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Clear user cart after successful order creation
        cart.clearCart();
        cartRepository.save(cart);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!user.getRole().equals(Role.ROLE_ADMIN) && !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied for requested order");
        }

        return mapToOrderResponse(order);
    }

    @Override
    public PagedResponse<OrderResponse> getUserOrders(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @Override
    public PagedResponse<OrderResponse> getAllOrdersAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusAdmin(Long orderId, OrderStatusUpdateRequest statusRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(statusRequest.getStatus());
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        User u = order.getUser();
        UserResponse userResponse = new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole(),
                u.getCreatedAt()
        );

        List<OrderItemResponse> itemResponses = order.getItems().stream()
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
                    return new OrderItemResponse(
                            item.getId(),
                            productResponse,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    );
                })
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                userResponse,
                itemResponses,
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
