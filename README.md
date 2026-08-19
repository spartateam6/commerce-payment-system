# 🛒 커머스 결제 시스템 프로젝트

> Spring Boot와 JPA를 활용한 커머스 주문·결제 시스템

회원이 상품을 조회하고 장바구니에 담아 주문·결제·취소까지 진행할 수 있으며,
재고 차감과 복구, 결제 상태 관리를 통해 **데이터 정합성을 보장하는 커머스 백엔드 시스템**입니다.

> 필수 과제에 대한 README 입니다

![Static Badge](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Static Badge](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Static Badge](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)

![Static Badge](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

![Static Badge](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

## Links

- [API 명세서 확인하기](./API_SPEC.md)
- [회의록 및 팀 컨벤션](https://github.com/spartateam6/commerce-payment-system/wiki)
- [README v1 (기본기 과제 README)](./README.v1.md)
---
## Team Code Convention

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

- Order

| Status | Description |
|---|---|
| `PAYMENT_PENDING` | 주문 생성 후 결제 대기 |
| `CONFIRMED` | 결제 완료 및 주문 확정 |
| `CANCELLED` | 주문 취소 또는 전액 환불 완료 |

- Payment

| Status | Description |
|---|---|
| `PENDING` | 결제 대기 |
| `PAID` | 결제 완료 |
| `FAILED` | 결제 실패 |
| `REFUNDED` | 전액 환불 완료 |

- Refund

| Status | Description |
|---|---|
| `COMPLETED` | 전액 환불 완료 |
| `FAILED` | 전액 환불 실패 |

- Point Transaction

| Type | Description |
|---|---|
| `USE` | 포인트 사용 |
| `EARN` | 포인트 적립 |
| `USE_RESTORE` | 사용 포인트 반환 |
| `EARN_REVOKE` | 적립 포인트 회수 |

- Webhook Event

| Status | Description |
|---|---|
| `RECEIVED` | 웹훅 수신 |
| `PROCESSED` | 정상 처리 완료 |
| `FAILED` | 처리 실패 |
| `IGNORED` | 중복 이벤트 또는 처리 불필요 |

- Product

| Status | Description |
|---|---|
| `ON_SALE` | 판매 중 |
| `DISCONTINUED` | 판매 중지 |

---
## 📌 ERD

<img width="1300" height="688" alt="image" src="https://github.com/user-attachments/assets/ccff4061-2162-40a2-ac9c-24cef1a94113" />

## 📌 Flowchart

## 주문 / 결제 처리 흐름

### 1. 상품 주문 + 재고 선차감

상품 주문 시 재고를 확인하고 비관적 락을 통해 동시 주문 상황에서도
재고 정합성을 보장하며, 결제 이전에 재고를 선차감합니다.

<details>
<summary><b>상품 주문 + 재고 선차감 흐름</b></summary>

<p align="center">
  <img width="1292" height="1286" alt="image" src="https://github.com/user-attachments/assets/60fb3747-bac1-4a70-9c44-07258fb58794" />
</p>

</details>

---

### 2. 일반 카드 결제

PortOne 결제 완료 후 서버에서 실제 결제 상태와 결제 금액을 검증하고,
검증이 완료되면 결제 및 주문 상태를 확정합니다.

<details>
<summary><b>일반 카드 결제 흐름</b></summary>

<p align="center">
  <img width="1214" height="1206" alt="image" src="https://github.com/user-attachments/assets/85110940-ca33-4f7f-8b74-56016acde02b" />
</p>

</details>

---

### 3. 포인트 + 카드 복합 결제

사용 포인트를 제외한 금액만 PG를 통해 결제하고,
결제 금액 검증 후 포인트 사용 및 적립 내역을 함께 처리합니다.

<details>
<summary><b>포인트 + 카드 복합 결제 상세 흐름</b></summary>

<br>

<p align="center">
  <img width="1634" height="1568" alt="image" src="https://github.com/user-attachments/assets/165f748a-315f-4165-aaa8-39cb31279643" />
</p>

</details>

---

### 4. 포인트 전액(PG 0원) 결제

주문 금액 전액을 포인트로 결제하는 경우 PG 결제 과정을 생략하고,
포인트 잔액 차감 및 포인트 원장 기록만으로 주문을 확정합니다.

<details>
<summary><b>포인트 전액(PG 0원) 결제 흐름</b></summary>

<br>

<p align="center">
  <img width="1630" height="1576" alt="image" src="https://github.com/user-attachments/assets/cbc354f7-aa36-4b88-9c91-80c9a291a244" />
</p>

</details>

---

### 5. 웹훅 <-> Client Confirm 멱등 동기화

Client Confirm API와 PortOne Webhook의 도착 순서가 달라지거나
동일한 웹훅이 재전송되더라도 중복 결제 처리가 발생하지 않도록 멱등성을 보장합니다.

<details>
<summary><b>웹훅 <-> Client Confirm 멱등 동기화</b></summary>
  
<p align="center">
  <img width="1402" height="1598" alt="image" src="https://github.com/user-attachments/assets/73b535eb-7d8a-4369-9d2a-1098cd8472b3" />
</p>

</details>

---

### 6. 포인트 잔액 ↔ 원장 동기화/음수 잔액 정책

환불 및 적립 포인트 회수 과정에서 회원의 현재 포인트 잔액과
포인트 거래 원장의 정합성을 검증하며, 회수할 포인트가 부족한 경우
음수 잔액을 허용하는 정책을 통해 원장과 실제 잔액의 일관성을 유지합니다.

<details>
<summary><b>포인트 회수 및 잔액 동기화 상세 흐름</b></summary>

<br>

<p align="center">
  <img width="1392" height="1486" alt="image" src="https://github.com/user-attachments/assets/468a64e6-6814-4456-99c8-c41e64b60bbe" />
</p>

</details>

# 🧑‍💻 Contributors

<a href="https://github.com/prjkmo112"><img src="https://github.com/prjkmo112.png?s=50" width="50px" alt="prjkmo112"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/bomin03"><img src="https://github.com/bomin03.png?s=50" width="50px" alt="bomin03"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/trex1004"><img src="https://github.com/trex1004.png?s=50" width="50px" alt="trex1004"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/yulimlvphs"><img src="https://github.com/yulimlvphs.png?s=50" width="50px" alt="yulimlvphs"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/chaeb0414-collab"><img src="https://github.com/chaeb0414-collab.png?s=50" width="50px" alt="chaeb0414-collab"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
