# API Endpoints - Mini Marketplace

Base URL (local): http://localhost:8080

Base API path: /api

## Conventions

- Content type: application/json
- Auth model: Spring Security session or token strategy (final implementation dependent)
- Error format: standardized by GlobalExceptionHandler
- Status code principles:
  - 200 OK for successful reads/updates
  - 201 Created for successful resource creation
  - 204 No Content for successful delete
  - 400 Bad Request for invalid payload
  - 401 Unauthorized for unauthenticated requests
  - 403 Forbidden for role/access violations
  - 404 Not Found for missing resources
  - 409 Conflict for duplicate or state conflict
  - 500 Internal Server Error for unhandled cases

## 1) Authentication

### POST /api/auth/register
Register a new user.

Request:
```json
{
  "fullName": "Buyer One",
  "email": "buyer@example.com",
  "password": "StrongPass123",
  "role": "BUYER"
}
```

Response:
- 201 Created
- 400 Bad Request (validation)
- 409 Conflict (email already exists)

### POST /api/auth/login
Authenticate a user.

Request:
```json
{
  "email": "buyer@example.com",
  "password": "StrongPass123"
}
```

Response:
- 200 OK
- 401 Unauthorized

### POST /api/auth/logout
Logout current user.

Response:
- 200 OK

## 2) Products

### GET /api/products
Public listing for buyers and sellers.

Query params (optional):
- page
- size
- sort
- q

Response:
- 200 OK

### GET /api/products/{id}
Get single product details.

Response:
- 200 OK
- 404 Not Found

### POST /api/products
Create product (SELLER or ADMIN).

Request:
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic mouse",
  "price": 999.99,
  "stock": 25,
  "category": "Electronics"
}
```

Response:
- 201 Created
- 400 Bad Request
- 403 Forbidden

### PUT /api/products/{id}
Update product (owner SELLER or ADMIN).

Response:
- 200 OK
- 403 Forbidden
- 404 Not Found

### DELETE /api/products/{id}
Delete product (owner SELLER or ADMIN).

Response:
- 204 No Content
- 403 Forbidden
- 404 Not Found

## 3) Orders

### POST /api/orders
Place order (BUYER).

Request:
```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

Response:
- 201 Created
- 400 Bad Request
- 403 Forbidden
- 409 Conflict (stock or state issue)

### GET /api/orders/my
Get logged-in buyer orders.

Response:
- 200 OK
- 403 Forbidden

### GET /api/orders/{id}
Get order details.

Access:
- BUYER: own order only
- SELLER: order involving own products (if implemented)
- ADMIN: all

Response:
- 200 OK
- 403 Forbidden
- 404 Not Found

## 4) Admin

### GET /api/admin/users
List all users (ADMIN only).

Response:
- 200 OK
- 403 Forbidden

### PATCH /api/admin/users/{id}/role
Update user role (ADMIN only).

Request:
```json
{
  "role": "SELLER"
}
```

Response:
- 200 OK
- 400 Bad Request
- 404 Not Found

### PATCH /api/admin/users/{id}/status
Activate/deactivate user (ADMIN only).

Request:
```json
{
  "active": false
}
```

Response:
- 200 OK
- 404 Not Found

## Postman Collection (Planned)

A Postman collection should include:
- Auth requests
- Product CRUD requests
- Order requests
- Admin requests
- Role-based negative test cases (403 checks)

## Notes

This endpoint list is the contract baseline. Update this file whenever endpoint names, payloads, or status codes change.
