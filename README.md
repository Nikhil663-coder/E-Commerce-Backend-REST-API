# E-Commerce Backend REST API

A production-grade, scalable E-Commerce Backend REST API built with **Java 17** and **Spring Boot 3.3.2**.

## Features

- **Authentication & Authorization**: Stateless JWT Authentication with Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`).
- **Product Catalog Management**: Browse products, search by keyword, filter by category, pagination & sorting, Admin CRUD operations.
- **Category Management**: Hierarchical categories with custom slugs, public browsing, and Admin CRUD.
- **Shopping Cart**: Real-time cart management, add items, update quantities with stock validation, remove items, clear cart.
- **Order Processing**: Checkout cart to place orders, transactional stock deduction, order history, and Admin status updates.
- **Global Error Handling**: Standardized JSON error envelopes with timestamp, HTTP status, message, and field-level validation errors.
- **OpenAPI 3.0 / Swagger UI**: Built-in interactive documentation accessible at `/swagger-ui.html`.
- **Database & Data Seeding**: In-memory H2 database with H2 Console at `/h2-console` and automatic sample data initialization.

---

## Tech Stack & Architecture

- **Language**: Java 17+
- **Framework**: Spring Boot 3.3.2
- **Security**: Spring Security 6 & JJWT (`0.12.5`)
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: H2 In-Memory Database (configurable for PostgreSQL/MySQL)
- **Validation**: Jakarta Bean Validation
- **Documentation**: SpringDoc OpenAPI 3.0 UI (`2.5.0`)
- **Build Tool**: Apache Maven (`mvnw.cmd`)

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher installed (`java -version`)

### Run Application

1. Clone or navigate to project directory:
   ```bash
   cd c:\Users\nikhi\Documents\java
   ```

2. Run Spring Boot application using Maven wrapper:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

3. Server will start on `http://localhost:8080`.

---

## Default Accounts (Pre-Seeded)

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@ecommerce.com` | `Admin@123` |
| **Customer** | `john@example.com` | `Password@123` |

---

## Interactive Documentation & Database Console

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:ecommercedb`
  - User: `sa`
  - Password: `password`

---

## Key API Endpoints Summary

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/login`: Authenticate and obtain JWT token
- `POST /api/v1/auth/register`: Register new customer account
- `GET /api/v1/auth/me`: Get active user profile details

### Product Catalog (`/api/v1/products` - Public)
- `GET /api/v1/products?page=0&size=10&sortBy=price&sortDir=asc&search=hoodie&categoryId=2`: Search & filter products
- `GET /api/v1/products/{id}`: Get product details

### Admin Products (`/api/v1/admin/products` - `ROLE_ADMIN`)
- `POST /api/v1/admin/products`: Create new product
- `PUT /api/v1/admin/products/{id}`: Update product details
- `DELETE /api/v1/admin/products/{id}`: Soft delete / deactivate product

### Categories (`/api/v1/categories` & `/api/v1/admin/categories`)
- `GET /api/v1/categories`: List all categories
- `GET /api/v1/categories/slug/{slug}`: Fetch category by slug
- `POST /api/v1/admin/categories`: Create category (Admin)
- `PUT /api/v1/admin/categories/{id}`: Update category (Admin)

### Shopping Cart (`/api/v1/cart` - `ROLE_USER` / `ROLE_ADMIN`)
- `GET /api/v1/cart`: View current user shopping cart
- `POST /api/v1/cart/items`: Add product item to cart
- `PUT /api/v1/cart/items/{itemId}`: Update item quantity
- `DELETE /api/v1/cart/items/{itemId}`: Remove item from cart
- `DELETE /api/v1/cart`: Clear entire cart

### Orders (`/api/v1/orders` & `/api/v1/admin/orders`)
- `POST /api/v1/orders`: Checkout cart and place order
- `GET /api/v1/orders/{id}`: Get order details by ID
- `GET /api/v1/orders`: User order history (paginated)
- `GET /api/v1/admin/orders`: View all customer orders (Admin)
- `PATCH /api/v1/admin/orders/{id}/status`: Update order status (Admin)

---

## Running Tests

Run full test suite:
```bash
.\mvnw.cmd test
```
