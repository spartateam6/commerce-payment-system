package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Transactional
    public void restoreOrderProductStock(Long orderId) {
        List<OrderItem> orderItemList = orderItemRepository.findByOrder_Id(orderId);

        for (OrderItem orderItem : orderItemList) {
            Product product = orderItem.getProduct();
            Integer stock = orderItem.getQuantity();

            product.addStock(stock);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public List<Long> getCartItemIds(Long orderId) {
        return orderItemRepository.findCartItemIdsByOrderId(orderId);
    }

}
