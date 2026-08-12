# 커머스 결제 시스템 프로젝트


## Team Convention

### 📌 Git branch

```
feature/signup
feature/product-list
feature/cart-add
```
### 📌 Git Commit
종류 | 의미
-- | --
feature | 새로운 기능
fix | 버그 수정
refactor | 동작 변경 없는 구조 개선
docs | 문서 수정
chore | 설정·빌드 작업


<!-- notionvc: 3e75a566-b7ca-4ad8-abc6-7e712d7e597e -->
#### Workflow

feature -> pr -> develop -> main

#### Branch 전략

- main 브랜치
  - 직접 Push 막기
  - PR review 최소 인원 1~3명
- develop 브랜치
  - 각자 작업한 내용은 여기로 merge

### 📌 패키지 구조

```
|- common
   |- dto
   |- response
   |- filter
   |- exception
|- config
|- domain
   |- ...
      |- controller
      |- service
      |- dto
      |- repository
      |- entity
```

### 코드

#### 결제

- 대기 : `PENDING`
- 완료 : `PAID`
- 실패 : `FAILED`
- 부분환불 : ``
- 취소 : `REFUND`

#### 주문

- 결제대기 : `PENDING`
- 주문완료 : `ORDERED`
- 주문취소 : `CANCELED`

## 📌 ERD

<img width="1341" alt="6조 팀플 - 프레임 2 (1)" src="https://github.com/user-attachments/assets/3a6f2468-b742-41c1-a9da-c74050cee286" />

## 📌 전체 API 목록

| 진행 상태 | 기능 분류 | 기능명 | HTTP Method | API Path | 담당자 |
|:---:|:---:|---|:---:|---|:---:|
| 시작 전 | 상품 | 상품 목록 조회 | `GET` | `/api/products` |  |
| 시작 전 | 상품 | 상품 상세 조회 | `GET` | `/api/products/{productId}` |  |
| 시작 전 | 회원 | 회원가입 | `POST` | `/api/auth/signup` |  |
| 시작 전 | 회원 | 로그인 및 JWT 발급 | `POST` | `/api/auth/login` |  |
| 시작 전 | 회원 | 내 정보 조회 | `GET` | `/api/members/me` |  |
| 시작 전 | 장바구니 | 상품 담기 | `POST` | `/api/cart/items` |  |
| 시작 전 | 장바구니 | 장바구니 조회 | `GET` | `/api/cart/items` |  |
| 시작 전 | 장바구니 | 상품 수량 변경 | `PATCH` | `/api/cart/items/{cartItemId}` |  |
| 시작 전 | 장바구니 | 상품 삭제 | `DELETE` | `/api/cart/items/{cartItemId}` |  |
| 시작 전 | 장바구니 | 장바구니 비우기 | `DELETE` | `/api/cart/items` |  |
| 시작 전 | 주문 | 주문 미리보기 | `GET` | `/api/orders/preview` |  |
| 시작 전 | 주문 | 주문 생성 | `POST` | `/api/orders` |  |
| 시작 전 | 주문 | 주문 전체 취소 | `POST` | `/api/orders/{orderId}/cancel` |  |
| 시작 전 | 주문 | 주문 목록 조회 | `GET` | `/api/orders` |  |
| 시작 전 | 주문 | 주문 상세 조회 | `GET` | `/api/orders/{orderId}` |  |
| 시작 전 | 결제 | 모의 결제 승인 | `POST` | `/api/payments/confirm` |  |

---

## 📌 Flowchart
<img width="688" height="666" alt="주문 생성 요청" src="https://github.com/user-attachments/assets/976574f9-e5f5-46ed-a508-3e5ac75bccfc" />
<img width="669" height="684" alt="모의 결제 요청" src="https://github.com/user-attachments/assets/f202893d-f353-4bb3-9113-9bd0f2659b3a" />
<img width="687" height="639" alt="주문 취소 요청" src="https://github.com/user-attachments/assets/10eccad2-3430-4ef7-8ad4-dd1a298c6a3d" />
