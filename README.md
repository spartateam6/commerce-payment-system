# 🛒 커머스 결제 시스템 프로젝트

> Spring Boot와 JPA를 활용한 커머스 주문·결제 시스템

회원이 상품을 조회하고 장바구니에 담아 주문·결제·취소까지 진행할 수 있으며,
재고 차감과 복구, 결제 상태 관리를 통해 **데이터 정합성을 보장하는 커머스 백엔드 시스템**입니다.

![Static Badge](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Static Badge](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Static Badge](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)

![Static Badge](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

![Static Badge](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

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

#### Enum

- 결제

  - 대기 : `PENDING`
  - 완료 : `PAID`
  - 실패 : `FAILED`
  - 부분환불 : ``
  - 취소 : `REFUND`

- 주문

  - 결제대기 : `PENDING`
  - 주문완료 : `ORDERED`
  - 주문취소 : `CANCELED`

#### 공통 응답 구조

```json
{
  "code": "<오류 코드>",
  "message": "<오류 메시지>",
  "data": "<데이터> (존재하지 않을 수도 있음)"
}
```

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

# 🧑‍💻 Contributors

<a href="https://github.com/prjkmo112"><img src="https://github.com/prjkmo112.png?s=50" width="50px" alt="prjkmo112"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/bomin03"><img src="https://github.com/bomin03.png?s=50" width="50px" alt="bomin03"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/trex1004"><img src="https://github.com/trex1004.png?s=50" width="50px" alt="trex1004"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/yulimlvphs"><img src="https://github.com/yulimlvphs.png?s=50" width="50px" alt="yulimlvphs"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/chaeb0414-collab"><img src="https://github.com/chaeb0414-collab.png?s=50" width="50px" alt="chaeb0414-collab"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
