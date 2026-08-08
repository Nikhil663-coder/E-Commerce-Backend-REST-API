package com.ecommerce.api.initializer;

import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.Role;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsersAndCarts();
        }

        if (categoryRepository.count() == 0) {
            seedCategoriesAndProducts();
        }
    }

    private void seedUsersAndCarts() {
        // 1. Seed Admin User
        User admin = new User(
                "admin@ecommerce.com",
                passwordEncoder.encode("Admin@123"),
                "Admin",
                "User",
                Role.ROLE_ADMIN
        );
        User savedAdmin = userRepository.save(admin);
        cartRepository.save(new Cart(savedAdmin));

        // 2. Seed Regular Customer
        User customer = new User(
                "john@example.com",
                passwordEncoder.encode("Password@123"),
                "John",
                "Doe",
                Role.ROLE_USER
        );
        User savedCustomer = userRepository.save(customer);
        cartRepository.save(new Cart(savedCustomer));

        System.out.println(">>> Sample Users and Carts initialized successfully.");
    }

    private void seedCategoriesAndProducts() {
        // Categories
        Category electronics = categoryRepository.save(new Category("Electronics", "Gadgets, Devices & Tech Accessories", "electronics"));
        Category clothing = categoryRepository.save(new Category("Fashion & Apparel", "Men and Women Clothing & Apparel", "fashion-apparel"));
        Category home = categoryRepository.save(new Category("Home & Kitchen", "Home appliances, decor, and kitchenware", "home-kitchen"));
        Category books = categoryRepository.save(new Category("Books & Media", "Bestsellers, technical guides, and fiction", "books-media"));

        // Products
        List<Product> products = Arrays.asList(
                new Product(
                        "Pro Wireless Noise-Canceling Headphones",
                        "High-fidelity Bluetooth 5.3 headphones with active noise cancellation and 30-hour battery life.",
                        new BigDecimal("299.99"),
                        50,
                        electronics,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                        "SKU-ELEC-001"
                ),
                new Product(
                        "Ultra HD Smart Watch Series 5",
                        "Waterproof smartwatch featuring AMOLED display, heart-rate sensor, GPS, and fitness tracking.",
                        new BigDecimal("199.50"),
                        75,
                        electronics,
                        "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
                        "SKU-ELEC-002"
                ),
                new Product(
                        "Organic Cotton Slim Fit Hoodie",
                        "Premium 100% organic cotton pullover hoodie with ribbed cuffs and adjustable drawstring.",
                        new BigDecimal("59.99"),
                        120,
                        clothing,
                        "https://images.unsplash.com/photo-1556905055-8f358a7a47b2",
                        "SKU-CLOTH-001"
                ),
                new Product(
                        "Classic Denim Jacket",
                        "Vintage washed blue denim jacket crafted from durable cotton twill.",
                        new BigDecimal("89.00"),
                        40,
                        clothing,
                        "https://images.unsplash.com/photo-1576995853123-5a10305d93c0",
                        "SKU-CLOTH-002"
                ),
                new Product(
                        "Automatic Espresso & Coffee Maker",
                        "15-bar Italian pump espresso machine with integrated milk frother for lattes & cappuccinos.",
                        new BigDecimal("349.00"),
                        25,
                        home,
                        "https://images.unsplash.com/photo-1517668808822-9ebe02f2a698",
                        "SKU-HOME-001"
                ),
                new Product(
                        "Clean Code: A Handbook of Agile Software Craftsmanship",
                        "Classic software engineering guide by Robert C. Martin focusing on readable and maintainable code.",
                        new BigDecimal("42.50"),
                        100,
                        books,
                        "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c",
                        "SKU-BOOK-001"
                )
        );

        productRepository.saveAll(products);
        System.out.println(">>> Sample Categories & Products initialized successfully.");
    }
}
