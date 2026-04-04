# API Endpoints Documentation

Base path: `/api`

Security model:

- Public: selected auth and product read endpoints
- Authenticated: protected by Spring Security
- Role-restricted: `ADMIN`, `SELLER`, `BUYER`

## 1. Authentication (`/api/auth`)

### `POST /api/auth/register`
- Access: Public
- Description: Register a new user
- Example body:
```json
{
  "fullName": "Buyer User",
  "email": "buyer@example.com",
  "password": "Password123",
  "role": "BUYER"
}
```
- Success: `201 Created`

### `POST /api/auth/login`
- Access: Public
- Description: Authenticate user
- Success: `200 OK`

### `POST /api/auth/logout`
- Access: Authenticated
- Description: Logout current user
- Success: `200 OK`

### `GET /api/auth/me`
- Access: Authenticated
- Description: Get current authenticated user summary
- Success: `200 OK`

## 2. Products (`/api/products`)

### `GET /api/products`
- Access: Public
- Description: List products
- Success: `200 OK`

### `GET /api/products/{id}`
- Access: Public
- Description: Get product details
- Success: `200 OK`
- Not found: `404 Not Found`

### `POST /api/products`
- Access: `SELLER` (or `ADMIN` by policy)
- Description: Create product
- Success: `201 Created`
- Forbidden: `403 Forbidden`

### `PUT /api/products/{id}`
- Access: `SELLER` (or `ADMIN` by policy)
- Description: Update product
- Success: `200 OK`
- Forbidden: `403 Forbidden`

### `DELETE /api/products/{id}`
- Access: `SELLER` (or `ADMIN` by policy)
- Description: Delete product
- Success: `204 No Content` or `200 OK` (implementation response wrapper)
- Forbidden: `403 Forbidden`

## 3. Cart (`/api/cart/me`)

### `GET /api/cart/me`
- Access: `BUYER`
- Description: Get current buyer cart
- Success: `200 OK`

### `POST /api/cart/me/items`
- Access: `BUYER`
- Description: Add item to cart
- Success: `201 Created`

### `PUT /api/cart/me/items/{cartItemId}`
- Access: `BUYER`
- Description: Update item quantity
- Success: `200 OK`

### `DELETE /api/cart/me/items/{cartItemId}`
- Access: `BUYER`
- Description: Remove one item
- Success: `200 OK` or `204 No Content`

### `DELETE /api/cart/me/items`
- Access: `BUYER`
- Description: Clear cart
- Success: `200 OK` or `204 No Content`

## 4. Orders (`/api/orders`)

### `POST /api/orders`
- Access: `BUYER`
- Description: Place order from cart
- Success: `201 Created`

### `GET /api/orders/me`
- Access: `BUYER`
- Description: List buyer orders
- Success: `200 OK`

## 5. Reviews (`/api/products/{productId}/reviews`)

### `GET /api/products/{productId}/reviews`
- Access: Public
- Description: List product reviews
- Success: `200 OK`

### `POST /api/products/{productId}/reviews`
- Access: `BUYER`
- Description: Add review
- Success: `201 Created`

### `PUT /api/products/{productId}/reviews/{reviewId}`
- Access: `BUYER`
- Description: Update own review
- Success: `200 OK`

### `DELETE /api/products/{productId}/reviews/{reviewId}`
- Access: `BUYER`
- Description: Delete own review
- Success: `200 OK` or `204 No Content`

## 6. Admin (`/api/admin`)

### `GET /api/admin/users`
- Access: `ADMIN`
- Description: List users
- Success: `200 OK`

### `GET /api/admin/users/{id}`
- Access: `ADMIN`
- Description: Get user details
- Success: `200 OK`

### `PUT /api/admin/users/{id}/role`
- Access: `ADMIN`
- Description: Update user role
- Success: `200 OK`

### `PUT /api/admin/users/{id}/deactivate`
- Access: `ADMIN`
- Description: Deactivate user
- Success: `200 OK`

## Error Pattern

Global exception handling returns consistent JSON error payloads. Access denial maps to `403 Forbidden`.
