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
Create/Place an order for the authenticated buyer.

**Authentication:** Required (Bearer Token)  
**Authorization:** BUYER, ADMIN roles only

Request:
```json
{
  "items": [
    { 
      "productId": 1, 
      "quantity": 2 
    },
    { 
      "productId": 3, 
      "quantity": 1 
    }
  ]
}
```

Request Validation:
- items: required, non-empty array, each item must have productId and quantity
- productId: required, must exist
- quantity: required, minimum 1

Response (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "buyerId": 5,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Laptop",
        "quantity": 2,
        "pricePerUnit": 999.99,
        "subtotal": 1999.98
      },
      {
        "id": 2,
        "productId": 3,
        "productName": "Mouse",
        "quantity": 1,
        "pricePerUnit": 29.99,
        "subtotal": 29.99
      }
    ],
    "totalPrice": 2029.97,
    "status": "PENDING",
    "createdAt": "2026-03-30T10:15:30",
    "updatedAt": "2026-03-30T10:15:30"
  },
  "message": "Order created successfully"
}
```

Error Responses:
- 400 Bad Request: Invalid order data or validation error
- 401 Unauthorized: Missing/invalid token
- 403 Forbidden: User lacks required role (BUYER/ADMIN)
- 404 Not Found: Product not found
- 409 Conflict: Insufficient product stock
  ```json
  {
    "success": false,
    "error": "Insufficient stock for: Laptop. Available: 1, Requested: 5",
    "timestamp": "2026-03-30T10:15:30"
  }
  ```

Implementation Notes:
- Stock is automatically deducted from products upon successful order creation
- All orders start with PENDING status
- Order is atomic: all items processed together or none at all
- Mappings: Order entity → OrderResponse DTO via OrderMapper

---

### GET /api/orders/me
Get all orders for the authenticated buyer with pagination.

**Authentication:** Required (Bearer Token)  
**Authorization:** BUYER, ADMIN roles only

Query Parameters (optional):
- page: Page number (0-indexed), default = 0
- size: Number of results per page, default = 10

Example Request:
```
GET /api/orders/me?page=0&size=10
Authorization: Bearer <token>
```

Response (200 OK):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "buyerId": 5,
        "items": [
          {
            "id": 1,
            "productId": 1,
            "productName": "Laptop",
            "quantity": 2,
            "pricePerUnit": 999.99,
            "subtotal": 1999.98
          }
        ],
        "totalPrice": 1999.98,
        "status": "PENDING",
        "createdAt": "2026-03-30T10:15:30",
        "updatedAt": "2026-03-30T10:15:30"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 2,
      "totalPages": 1
    }
  },
  "message": "Orders retrieved successfully"
}
```

Error Responses:
- 401 Unauthorized: Missing/invalid token
- 403 Forbidden: User lacks required role
- 404 Not Found: Buyer not found

Implementation Notes:
- Returns only orders for authenticated buyer
- ADMIN role can retrieve any buyer's orders (future enhancement)
- Results include nested order items with product details
- Pagination reduces payload size for large order histories

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
