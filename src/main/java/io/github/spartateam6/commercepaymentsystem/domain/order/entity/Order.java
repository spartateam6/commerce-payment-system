package io.github.spartateam6.commercepaymentsystem.domain.order.entity;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode.ORDER_MEMBER_ID_REQUIRED;
import static io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode.ORDER_NUMBER_REQUIRED;

@Getter
@Setter
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_order_number",
                        columnNames = "order_number"
                )
        },
        indexes = {
                @Index(
                        name = "idx_orders_member_created_at",
                        columnList = "member_id, created_at"
                )
        }
)
// TODO : 추후에 PROTECTED로 바꾸기
// TODO : setter 추후에 제거하겠습니다.
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Order extends AuditingEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "order_id")
        private Long id;

        @Column(name = "member_id", nullable = false)
        private Long memberId;

        @Column(name = "order_number", nullable = false, length = 50)
        private String orderNumber;

        @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
        private BigDecimal totalAmount;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 30)
        private OrderStatus status;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
        @OrderBy("id ASC")
        private List<OrderItem> orderItems = new ArrayList<>();

        public static Order create(
                Long memberId,
                String orderNumber
        ) {
            if (memberId == null) {
                throw new BusinessException(ORDER_MEMBER_ID_REQUIRED);
            }

            if (orderNumber == null || orderNumber.isBlank()) {
                throw new BusinessException(ORDER_NUMBER_REQUIRED);
            }

            Order order = new Order();

            order.memberId = memberId;
            order.orderNumber = orderNumber;
            order.totalAmount = BigDecimal.ZERO;
            order.status = OrderStatus.PAYMENT_PENDING;

            return order;
        }


         // OrderItem 생성과 연관관계 설정을 Order가 관리한다.
         // //주문 상품이 추가될 때 주문 총액도 함께 증가한다.
        public void addOrderItem(
                Long productId,
                String productName,
                BigDecimal unitPrice,
                Integer quantity
        ) {
            OrderItem orderItem = OrderItem.create(
                    this,
                    productId,
                    productName,
                    unitPrice,
                    quantity
            );

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(
                    orderItem.calculateLineAmount()
            );
        }

        // 외부에서 orderItems 컬렉션을 직접 변경하지 못하게 한다.
        public List<OrderItem> getOrderItems() {
            return Collections.unmodifiableList(orderItems);
        }
}