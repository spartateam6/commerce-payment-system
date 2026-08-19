# API 명세서 — commerce-payment-system

## 목차

- [공통 사항](#공통-사항)
- [인증 (Auth)](#1-인증-auth)
- [회원 (Member)](#2-회원-member)
- [상품 (Product)](#3-상품-product)
- [장바구니 (Cart)](#4-장바구니-cart)
- [주문 (Order)](#5-주문-order)
- [결제 (Payment)](#6-결제-payment)
- [에러 코드](#에러-코드)

---

## 공통 사항

### Base URL

```
/api
```

### 공통 응답 포맷

모든 응답은 아래 형태로 감싸져 내려갑니다. (`ApiResponse<T>`)

**성공**

```json
{
  "code": "SUCCESS",
  "data": { }
}
```

**실패**

```json
{
  "code": "PRODUCT_002",
  "message": "재고가 부족합니다."
}
```

> `data`, `message`는 값이 없으면 응답 JSON에서 생략됩니다. (`@JsonInclude(NON_NULL)`)

### 인증 방식

로그인 이후 발급받은 JWT를 `Authorization` 헤더에 `Bearer` 스킴으로 담아 요청합니다.

```
Authorization: Bearer {accessToken}
```

- 아래 경로는 인증 없이 호출 가능합니다: `/api/auth/**`, `/api/products/**`
- 그 외 모든 API는 인증이 필요하며, `memberId`는 **서버가 JWT에서 직접 추출**합니다. 클라이언트가 body/param으로 넘긴 값은 신뢰하지 않습니다.
- 인증 실패 시 `401 Unauthorized` (`AUTH_001` / `AUTH_002`)가 반환됩니다.

---

## 1. 인증 (Auth)

### 1-1. 회원가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/auth/signup` |
| 인증 | 불필요 |
| 성공 응답 | `201 Created` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | O | 이메일 형식, 최대 255자 |
| `password` | String | O | 영문+숫자+특수문자 포함 8자 이상 |
| `name` | String | O | 공백 불가 |
| `phoneNumber` | String | O | `010-0000-0000` 형식 |

```json
{
  "email": "user@example.com",
  "password": "abcd1234!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

**Response Body**

```json
{
  "code": "SUCCESS"
}
```

---

### 1-2. 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/auth/login` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | O | 이메일 형식, 최대 255자 |
| `password` | String | O | 영문+숫자+특수문자 포함 8자 이상 |

```json
{
  "email": "user@example.com",
  "password": "abcd1234!"
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

---

## 2. 회원 (Member)

### 2-1. 내 정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/members/me` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "email": "user@example.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }
}
```

---

## 3. 상품 (Product)

### 3-1. 상품 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/products` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `category` | String | X | - | 카테고리 필터 |
| `minPrice` | Integer | X | - | 최소 가격 필터 |
| `maxPrice` | Integer | X | - | 최대 가격 필터 |
| `page` | int | X | `1` | 페이지 번호 (1부터 시작) |
| `size` | int | X | `10` | 페이지 크기 |

예: `GET /api/products?category=food&minPrice=1000&maxPrice=50000&page=1&size=10`

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "상품명",
        "price": 10000,
        "stock": 25,
        "description": "상품 설명",
        "category": "food",
        "soldOut": false,
        "createdAt": "2026-08-01T10:00:00",
        "updatedAt": "2026-08-01T10:00:00"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

---

### 3-2. 상품 단건 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/products/{productId}` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `productId` | Long | 상품 ID |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "id": 1,
    "name": "상품명",
    "price": 10000,
    "stock": 25,
    "description": "상품 설명",
    "category": "food",
    "soldOut": false,
    "createdAt": "2026-08-01T10:00:00",
    "updatedAt": "2026-08-01T10:00:00"
  }
}
```

---

## 4. 장바구니 (Cart)

### 4-1. 장바구니 상품 추가

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `201 Created` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `productId` | Long | O | - |
| `quantity` | Integer | O | 1 이상 |

```json
{
  "productId": 1,
  "quantity": 2
}
```

> 동일 상품을 다시 추가하면 기존 행에 수량이 합산됩니다(중복 행 생성 안 됨).

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartItemId": 10,
    "productId": 1,
    "productName": "상품명",
    "price": 10000,
    "quantity": 2,
    "totalPrice": 20000
  }
}
```

---

### 4-2. 장바구니 상품 수량 변경

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| URL | `/api/cart/items/{cartItemId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `cartItemId` | Long | 장바구니 항목 ID |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `quantity` | Integer | O | - |

```json
{
  "quantity": 3
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartItemId": 10,
    "productId": 1,
    "productName": "상품명",
    "price": 10000,
    "quantity": 3,
    "totalPrice": 30000
  }
}
```

---

### 4-3. 장바구니 상품 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| URL | `/api/cart/items/{cartItemId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `204 No Content` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `cartItemId` | Long | 장바구니 항목 ID |

**Request / Response Body**: 없음

---

### 4-4. 장바구니 비우기

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `204 No Content` |

**Request / Response Body**: 없음

---

### 4-5. 장바구니 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartId": 5,
    "items": [
      {
        "cartItemId": 10,
        "productId": 1,
        "productName": "상품명",
        "price": 10000,
        "quantity": 2,
        "totalPrice": 20000
      }
    ],
    "totalPrice": 20000
  }
}
```

---

## 5. 주문 (Order)

### 5-1. 주문 미리보기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders/preview` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `cartItemIds` | List\<Long\> | O | 각 원소 양수 |

```json
{
  "cartItemIds": [10, 11]
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "items": [
      {
        "cartItemId": 10,
        "productId": 1,
        "productName": "상품명",
        "unitPrice": 10000,
        "quantity": 2,
        "lineAmount": 20000
      }
    ],
    "totalAmount": 20000
  }
}
```

---

### 5-2. 주문 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders` |
| 인증 | 필요 |
| 성공 응답 | `201 Created` (`Location: /api/orders/{orderId}`) |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `cartItemIds` | List\<Long\> | O | 각 원소 양수 |

```json
{
  "cartItemIds": [10, 11]
}
```

**동작 개요**: 재고 검증 → 선차감 → `Order`/`OrderItem` 스냅샷 생성 → `Payment(PENDING)` 생성. 하나라도 재고가 부족하면 전체 롤백됩니다. 주문 생성만으로는 장바구니가 비워지지 않습니다.

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260819-0001",
    "totalAmount": 20000
  }
}
```

---

### 5-3. 주문 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/orders/{orderId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `orderId` | Long | 주문 ID |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260819-0001",
    "totalAmount": 20000,
    "orderStatus": "PAYMENT_PENDING",
    "createdAt": "2026-08-19T10:00:00",
    "updatedAt": "2026-08-19T10:00:00",
    "orderItems": [
      {
        "orderItemId": 200,
        "productId": 1,
        "productName": "상품명",
        "unitPrice": 10000,
        "quantity": 2,
        "lineAmount": 20000,
        "createdAt": "2026-08-19T10:00:00"
      }
    ],
    "payment": {
      "paymentId": 300,
      "amount": 20000,
      "status": "PENDING",
      "completedAt": null,
      "createdAt": "2026-08-19T10:00:00"
    }
  }
}
```

> `orderStatus`는 `PAYMENT_PENDING` / `CONFIRMED` / `CANCELLED` / `FAILED` 중 하나입니다.

---

### 5-4. 주문 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/orders` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Query Parameters** (Spring `Pageable`)

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | `0` | 페이지 번호 (0부터 시작) |
| `size` | int | `20` | 페이지 크기 |
| `sort` | String | `createdAt,DESC` | 정렬 기준 |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": [
    {
      "orderId": 100,
      "orderNumber": "ORD-20260819-0001",
      "totalAmount": 20000,
      "orderStatus": "CONFIRMED",
      "createdAt": "2026-08-19T10:00:00",
      "updatedAt": "2026-08-19T10:05:00",
      "orderItems": [ ],
      "payment": { }
    }
  ]
}
```

---

### 5-5. 주문 취소

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders/{orderId}/cancel` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `orderId` | Long | 주문 ID |

**동작 개요**:
- 결제 전(`PAYMENT_PENDING`/`PENDING`) 취소 → `Order = CANCELLED`, `Payment = FAILED`, 재고 전체 복구
- 결제 후(`CONFIRMED`/`PAID`) 취소 → `Order = CANCELLED`, `Payment = REFUND`, 재고 전체 복구
- 이미 취소된 주문은 `409 Conflict` (`ORDER_010`)로 거부되어 중복 복구를 방지합니다.

**Request / Response Body**

```json
{
  "code": "SUCCESS"
}
```

---

## 6. 결제 (Payment)

### 6-1. 모의 결제 확정

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/payments/confirm` |
| 인증 | 필요 (본인 주문 검증) |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `orderNumber` | String | O | 공백 불가 |
| `price` | Integer | O | 1 이상 |

```json
{
  "orderNumber": "ORD-20260819-0001",
  "price": 20000
}
```

> `price`는 DB에 저장된 `Payment.amount`와의 검증용 값일 뿐, 실제 승인 여부(SUCCESS/FAIL)는 서버 로직이 판단합니다.

**동작 개요** (단일 트랜잭션):

| 결과 | Payment | Order | 재고 | 장바구니 |
|---|---|---|---|---|
| SUCCESS | `PENDING → PAID`, `completedAt` 기록 | `PAYMENT_PENDING → CONFIRMED` | 변경 없음 | 비움 |
| FAIL | `PENDING → FAILED` | `PAYMENT_PENDING → FAILED` | 전체 복구 | 유지 |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "status": "ok"
  }
}
```

**실패 시** (PG 결제 미승인, `PAYMENT_004`)

```json
{
  "code": "PAYMENT_004",
  "message": "PG사 결제가 완료되지 않았습니다."
}
```
