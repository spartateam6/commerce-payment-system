-- ---------------------------------------------------------
-- 테스트 데이터 초기화
-- FK 때문에 자식 테이블부터 삭제
-- ---------------------------------------------------------

DELETE FROM order_item;
DELETE FROM payment;
DELETE FROM `order`;
DELETE FROM cart_items;
DELETE FROM cart;
DELETE FROM product;
DELETE FROM member;


-- ---------------------------------------------------------
-- 1. MEMBER
-- ---------------------------------------------------------

INSERT INTO member (
    id,
    created_at,
    updated_at,
    email,
    name,
    password,
    phone_number
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        'user1@test.com',
        '테스트유저1',
        'TEST_PASSWORD',
        '010-1111-1111'
    ),
    (
        2,
        NOW(6),
        NOW(6),
        'user2@test.com',
        '테스트유저2',
        'TEST_PASSWORD',
        '010-2222-2222'
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
    stock
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
        8
    ),
    (
        2,
        NOW(6),
        NOW(6),
        'CLOTHES',
        '기본 청바지',
        '청바지',
        30000,
        4
    ),
    (
        3,
        NOW(6),
        NOW(6),
        'FOOD',
        '테스트용 사과',
        '사과',
        12000,
        19
    ),
    (
        4,
        NOW(6),
        NOW(6),
        'ETC',
        '테스트용 텀블러',
        '텀블러',
        20000,
        0
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
-- 5. ORDER
-- ---------------------------------------------------------

INSERT INTO `order` (
    order_id,
    created_at,
    updated_at,
    order_number,
    price_total,
    status,
    member_id
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        'ORDER-20260814-0001',
        30000,
        'PENDING',
        1
    ),
    (
        2,
        NOW(6),
        NOW(6),
        'ORDER-20260814-0002',
        30000,
        'COMPLETED',
        1
    ),
    (
        3,
        NOW(6),
        NOW(6),
        'ORDER-20260814-0003',
        12000,
        'PENDING',
        2
    );


-- ---------------------------------------------------------
-- 6. ORDER_ITEM
-- ---------------------------------------------------------

INSERT INTO order_item (
    order_item_id,
    created_at,
    updated_at,
    price,
    product_name,
    quantity,
    order_id,
    product_id
)
VALUES
    -- ORDER 1
    (
        1,
        NOW(6),
        NOW(6),
        15000,
        '반팔 티셔츠',
        2,
        1,
        1
    ),

    -- ORDER 2
    (
        2,
        NOW(6),
        NOW(6),
        30000,
        '청바지',
        1,
        2,
        2
    ),

    -- ORDER 3
    (
        3,
        NOW(6),
        NOW(6),
        12000,
        '사과',
        1,
        3,
        3
    );


-- ---------------------------------------------------------
-- 7. PAYMENT
-- ---------------------------------------------------------

INSERT INTO payment (
    id,
    created_at,
    updated_at,
    amount,
    completed_at,
    status,
    order_id
)
VALUES
    (
        1,
        NOW(6),
        NOW(6),
        30000,
        NULL,
        'PENDING',
        1
    ),
    (
        2,
        NOW(6),
        NOW(6),
        30000,
        NOW(6),
        'PAID',
        2
    ),
    (
        3,
        NOW(6),
        NOW(6),
        12000,
        NULL,
        'PENDING',
        3
    );
