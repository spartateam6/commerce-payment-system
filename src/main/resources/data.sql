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
    ),
    (
        3,
        NOW(6),
        NOW(6),
        'user@example.com',
        '포인트많은유저',
        '$2a$12$GeifU8Kqme5IKtQa9aVAN.CWXCRjHNwIlYssY64Hw.VCBpDnu6wky',
        '010-3333-3333',
        1000000
    )
;


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
        1500,
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
        3000,
        4,
        'ON_SALE'
    ),
    (3,
     NOW(6),
     NOW(6),
     'FOOD',
     '테스트용 사과',
     '사과',
     1200,
     19,
     'ON_SALE'
    ),
    (4,
     NOW(6),
     NOW(6),
     'ETC',
     '테스트용 텀블러',
     '텀블러',
     2000,
     0,
     'ON_SALE'
    ),
    (5,
     NOW(6),
     NOW(6),
     'CLOTHES',
     '작년 시즌 재킷',
     '재킷',
     8900,
     0,
     'DISCONTINUED'
    ),
    (6,  NOW(6), NOW(6), 'CLOTHES',     '오버핏 무지 후드티',           '후드티',              4500,  15, 'ON_SALE'),
    (7,  NOW(6), NOW(6), 'CLOTHES',     '슬림핏 치노 팬츠',             '치노 팬츠',            3800,  10, 'ON_SALE'),
    (8,  NOW(6), NOW(6), 'CLOTHES',     '루즈핏 린넨 셔츠',             '린넨 셔츠',            5200,   7, 'ON_SALE'),
    (9,  NOW(6), NOW(6), 'CLOTHES',     '크루넥 니트 스웨터',           '니트 스웨터',          5800,   3, 'ON_SALE'),
    (10, NOW(6), NOW(6), 'CLOTHES',     '기모 맨투맨 티셔츠',           '기모 맨투맨',          3500,  20, 'ON_SALE'),
    (11, NOW(6), NOW(6), 'SHOES',       '경량 런닝화 (화이트)',          '런닝화',               8900,   6, 'ON_SALE'),
    (12, NOW(6), NOW(6), 'SHOES',       '캐주얼 슬립온 스니커즈',        '슬립온 스니커즈',      6200,   9, 'ON_SALE'),
    (13, NOW(6), NOW(6), 'SHOES',       '여름용 스트랩 샌들',           '스트랩 샌들',          2900,   0, 'ON_SALE'),
    (14, NOW(6), NOW(6), 'ACCESSORY',   '캔버스 토트백',                '캔버스 토트백',         2500,  18, 'ON_SALE'),
    (15, NOW(6), NOW(6), 'ACCESSORY',   '빈티지 프레임 선글라스',        '빈티지 선글라스',      4200,   4, 'ON_SALE'),
    (16, NOW(6), NOW(6), 'ACCESSORY',   '미니멀 가죽 지갑',             '가죽 지갑',            5500,   8, 'ON_SALE'),
    (17, NOW(6), NOW(6), 'ELECTRONICS', '완전무선 블루투스 이어폰',      '블루투스 이어폰',     12900,   5, 'ON_SALE'),
    (18, NOW(6), NOW(6), 'ELECTRONICS', '14인치 노트북 슬리브 파우치',   '노트북 파우치',        2800,  22, 'ON_SALE'),
    (19, NOW(6), NOW(6), 'ELECTRONICS', '60W PD 고속 충전기',           '고속 충전기',          1900,  30, 'ON_SALE'),
    (20, NOW(6), NOW(6), 'FOOD',        '유기농 그래놀라 500g',          '유기농 그래놀라',       8900,  40, 'ON_SALE'),
    (21, NOW(6), NOW(6), 'FOOD',        '콜드브루 원두 블렌드 200g',     '콜드브루 원두',        1800,  15, 'ON_SALE'),
    (22, NOW(6), NOW(6), 'FOOD',        '마카다미아 견과류 믹스 300g',   '견과류 믹스',          12500,  25, 'ON_SALE'),
    (23, NOW(6), NOW(6), 'BEAUTY',      '수분 진정 스킨케어 4종 세트',   '스킨케어 세트',        7600,   5, 'ON_SALE'),
    (24, NOW(6), NOW(6), 'BEAUTY',      'SPF 50+ 가벼운 선크림 50ml',   '데일리 선크림',        2200,  12, 'ON_SALE'),
    (25, NOW(6), NOW(6), 'SPORTS',      'TPE 논슬립 요가매트 6mm',       '요가매트',             3500,   7, 'ON_SALE'),
    (26, NOW(6), NOW(6), 'SPORTS',      '웨이 프로틴 초코맛 1kg',        '웨이 프로틴',          4500,   0, 'DISCONTINUED'),
    (27, NOW(6), NOW(6), 'ETC',         '소이 캔들 3종 기프트 세트',     '소이 캔들 세트',       3200,  11, 'ON_SALE');


-- ---------------------------------------------------------
-- 3. CART
-- 회원당 장바구니 하나
-- ---------------------------------------------------------

# INSERT INTO cart (
#     cart_id,
#     created_at,
#     updated_at,
#     member_id
# )
# VALUES
#     (1, NOW(6), NOW(6), 1),
#     (2, NOW(6), NOW(6), 2);
#
#
# -- ---------------------------------------------------------
# -- 4. CART_ITEMS
# -- ---------------------------------------------------------
#
# INSERT INTO cart_items (
#     cart_items_id,
#     created_at,
#     updated_at,
#     quantity,
#     cart_id,
#     product_id
# )
# VALUES
#     (1, NOW(6), NOW(6), 2, 1, 1),
#     (2, NOW(6), NOW(6), 1, 1, 3),
#     (3, NOW(6), NOW(6), 1, 2, 3);
#
# -- ---------------------------------------------------------
# -- 5. ORDERS
# -- ---------------------------------------------------------
#
# INSERT INTO orders (
#     order_id,
#     created_at,
#     updated_at,
#     member_id,
#     order_number,
#     total_amount,
#     status
# )
# VALUES
#     (
#         1,
#         NOW(6),
#         NOW(6),
#         1,
#         'ORD-20260814-0001',
#         30000.00,
#         'PAYMENT_PENDING'
#     ),
#     (
#         2,
#         NOW(6),
#         NOW(6),
#         1,
#         'ORD-20260814-0002',
#         30000.00,
#         'CONFIRMED'
#     ),
#     (
#         3,
#         NOW(6),
#         NOW(6),
#         2,
#         'ORD-20260814-0003',
#         12000.00,
#         'PAYMENT_PENDING'
#     );
#
#
# -- ---------------------------------------------------------
# -- 6. ORDER_ITEMS
# -- ---------------------------------------------------------
#
# INSERT INTO order_items (
#     order_item_id,
#     created_at,
#     updated_at,
#     order_id,
#     product_id,
#     product_name_snapshot,
#     unit_price_snapshot,
#     quantity
# )
# VALUES
#     -- 주문 1: 반팔 티셔츠 15,000원 × 2개 = 30,000원
#     (
#         1,
#         NOW(6),
#         NOW(6),
#         1,
#         1,
#         '반팔 티셔츠',
#         15000.00,
#         2
#     ),
#
#     -- 주문 2: 청바지 30,000원 × 1개 = 30,000원
#     (
#         2,
#         NOW(6),
#         NOW(6),
#         2,
#         2,
#         '청바지',
#         30000.00,
#         1
#     ),
#
#     -- 주문 3: 사과 12,000원 × 1개 = 12,000원
#     (
#         3,
#         NOW(6),
#         NOW(6),
#         3,
#         3,
#         '사과',
#         12000.00,
#         1
#     );
#
# -- ---------------------------------------------------------
# -- 7. PAYMENT
# -- ---------------------------------------------------------
#
# INSERT INTO payment (
#     id,
#     created_at,
#     updated_at,
#     order_amount,
#     pg_amount,
#     portone_payment_id,
#     completed_at,
#     status,
#     order_id
# )
# VALUES
#     (1, NOW(6), NOW(6), 30000, 30000, "123", NULL, 'PENDING', 1),
#     (2, NOW(6), NOW(6), 30000, 25000, "234", NOW(6), 'PAID', 2),
#     (3, NOW(6), NOW(6), 12000, 12000, "456", NULL, 'PENDING', 3);
#
# -- ---------------------------------------------------------
# -- ---------------------------------------------------------
# -- 8. POINT_TRANSACTION (원장)
# -- 잔액 == SUM(amount)가 항상 성립하도록 초기 지급도 원장에 남긴다.
# -- user1: 초기 지급 10,000 → 주문 2번에서 5,000 사용(PG 25,000 결제) → 1% 적립 250
# --        합계 5,250 = member.point_balance
# -- 초기 지급은 연결된 결제가 없어 payment_id가 NULL이다.
# -- ---------------------------------------------------------
#
# INSERT INTO point_transactions (member_id, payment_id, transaction_type, amount, created_at, updated_at)
# VALUES (1, NULL, 'EARN', 10000, NOW(6), NOW(6)),
#        (1, 2, 'USE', -5000, NOW(6), NOW(6)),
#        (1, 2, 'EARN', 250, NOW(6), NOW(6)),
#        (3, NULL, 'EARN', 1000000, NOW(6), NOW(6));