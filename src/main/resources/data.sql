-- ---------------------------------------------------------
-- 테스트 데이터 초기화
-- FK 때문에 자식 테이블부터 삭제
-- ---------------------------------------------------------

DELETE FROM point_transactions;
DELETE FROM order_items;
DELETE FROM payment;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM cart;
DELETE FROM product;
DELETE FROM member;


-- ---------------------------------------------------------
-- 1. MEMBER
-- 테스트 계정 비번은 모두 testpwd1! 입니다
-- ---------------------------------------------------------

INSERT INTO member (
    id,
    created_at,
    updated_at,
    email,
    name,
    password,
    phone_number,
    point_balance
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        'user1@test.com',
        '테스트유저1',
        '$2a$12$GeifU8Kqme5IKtQa9aVAN.CWXCRjHNwIlYssY64Hw.VCBpDnu6wky',
        '010-1111-1111',
        5250
    ),
    (
        2,
        NOW(6),
        NOW(6),
        'user2@test.com',
        '테스트유저2',
        '$2a$12$GeifU8Kqme5IKtQa9aVAN.CWXCRjHNwIlYssY64Hw.VCBpDnu6wky',
        '010-2222-2222',
        0
    );


-- ---------------------------------------------------------
-- 2. PRODUCT
-- ---------------------------------------------------------

INSERT INTO product (
    id,
    created_at,
    updated_at,
    category,
    description,
    name,
    price,
    stock,
    sale_status
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        'CLOTHES',
        '기본 반팔 티셔츠',
        '반팔 티셔츠',
        15000,
        8,
        'ON_SALE'
    ),
    (
        2,
        NOW(6),
        NOW(6),
        'CLOTHES',
        '기본 청바지',
        '청바지',
        30000,
        4,
        'ON_SALE'
    ),
    (3,
     NOW(6),
     NOW(6),
     'FOOD',
     '테스트용 사과',
     '사과',
     12000,
     19,
     'ON_SALE'
    ),
    (4,
     NOW(6),
     NOW(6),
     'ETC',
     '테스트용 텀블러',
     '텀블러',
     20000,
     0,
     'ON_SALE'
    ),
    (5,
     NOW(6),
     NOW(6),
     'CLOTHES',
     '작년 시즌 재킷',
     '재킷',
     89000,
     0,
     'DISCONTINUED'
    );


-- ---------------------------------------------------------
-- 3. CART
-- 회원당 장바구니 하나
-- ---------------------------------------------------------

INSERT INTO cart (
    cart_id,
    created_at,
    updated_at,
    member_id
)
VALUES
    (1, NOW(6), NOW(6), 1),
    (2, NOW(6), NOW(6), 2);


-- ---------------------------------------------------------
-- 4. CART_ITEMS
-- ---------------------------------------------------------

INSERT INTO cart_items (
    cart_items_id,
    created_at,
    updated_at,
    quantity,
    cart_id,
    product_id
)
VALUES
    (1, NOW(6), NOW(6), 2, 1, 1),
    (2, NOW(6), NOW(6), 1, 1, 3),
    (3, NOW(6), NOW(6), 1, 2, 3);

-- ---------------------------------------------------------
-- 5. ORDERS
-- ---------------------------------------------------------

INSERT INTO orders (
    order_id,
    created_at,
    updated_at,
    member_id,
    order_number,
    total_amount,
    status
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        1,
        'ORD-20260814-0001',
        30000.00,
        'PAYMENT_PENDING'
    ),
    (
        2,
        NOW(6),
        NOW(6),
        1,
        'ORD-20260814-0002',
        30000.00,
        'CONFIRMED'
    ),
    (
        3,
        NOW(6),
        NOW(6),
        2,
        'ORD-20260814-0003',
        12000.00,
        'PAYMENT_PENDING'
    );


-- ---------------------------------------------------------
-- 6. ORDER_ITEMS
-- ---------------------------------------------------------

INSERT INTO order_items (
    order_item_id,
    created_at,
    updated_at,
    order_id,
    product_id,
    product_name_snapshot,
    unit_price_snapshot,
    quantity
)
VALUES
    -- 주문 1: 반팔 티셔츠 15,000원 × 2개 = 30,000원
    (
        1,
        NOW(6),
        NOW(6),
        1,
        1,
        '반팔 티셔츠',
        15000.00,
        2
    ),

    -- 주문 2: 청바지 30,000원 × 1개 = 30,000원
    (
        2,
        NOW(6),
        NOW(6),
        2,
        2,
        '청바지',
        30000.00,
        1
    ),

    -- 주문 3: 사과 12,000원 × 1개 = 12,000원
    (
        3,
        NOW(6),
        NOW(6),
        3,
        3,
        '사과',
        12000.00,
        1
    );

-- ---------------------------------------------------------
-- 7. PAYMENT
-- ---------------------------------------------------------

INSERT INTO payment (
    id,
    created_at,
    updated_at,
    order_amount,
    used_point_amount,
    pg_amount,
    completed_at,
    status,
    order_id
)
VALUES
    (1, NOW(6), NOW(6), 30000, 0, 30000, NULL, 'PENDING', 1),
    (2, NOW(6), NOW(6), 30000, 5000, 25000, NOW(6), 'PAID', 2),
    (3, NOW(6), NOW(6), 12000, 0, 12000, NULL, 'PENDING', 3);
-- ---------------------------------------------------------
-- 8. POINT_TRANSACTION (원장)
-- 잔액 == SUM(amount)가 항상 성립하도록 초기 지급도 원장에 남긴다.
-- user1: 초기 지급 10,000 → 주문 2번에서 5,000 사용(PG 25,000 결제) → 1% 적립 250
--        합계 5,250 = member.point_balance
-- 초기 지급은 연결된 결제가 없어 payment_id가 NULL이다.
-- ---------------------------------------------------------

INSERT INTO point_transactions (member_id, payment_id, transaction_type, amount, created_at, updated_at)
VALUES (1, NULL, 'EARN', 10000, NOW(6), NOW(6)),
       (1, 2, 'USE', -5000, NOW(6), NOW(6)),
       (1, 2, 'EARN', 250, NOW(6), NOW(6));