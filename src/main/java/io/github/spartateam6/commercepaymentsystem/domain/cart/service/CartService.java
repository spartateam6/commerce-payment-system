package io.github.spartateam6.commercepaymentsystem.domain.cart.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemAddRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.Cart;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartRepository;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // TODO: MemberRepository와 ProductRepository 병합 후 제거
    private final EntityManager entityManager;

    @Transactional
    public CartItemResponse addItem(
            Long memberId,
            CartItemAddRequest request
    ) {
        // TODO: MemberRepository.findById()로 교체
        Member member = entityManager.find(
                Member.class,
                memberId
        );

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원을 찾을 수 없습니다."
            );
        }

        // TODO: ProductRepository.findById()로 교체
        Product product = entityManager.find(
                Product.class,
                request.productId()
        );

        if (product == null) {
            throw new IllegalArgumentException(
                    "상품을 찾을 수 없습니다."
            );
        }

        Cart cart = cartRepository.findByMember_Id(memberId)
                .orElseGet(() ->
                        cartRepository.save(
                                Cart.create(member)
                        )
                );

        Optional<CartItem> optionalCartItem =
                cartItemRepository.findByCart_IdAndProduct_Id(
                        cart.getId(),
                        product.getId()
                );

        int currentQuantity = optionalCartItem
                .map(CartItem::getQuantity)
                .orElse(0);

        int finalQuantity =
                currentQuantity + request.quantity();

        if (finalQuantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "요청 수량이 상품 재고를 초과합니다."
            );
        }

        CartItem cartItem;

        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.increaseQuantity(request.quantity());
        } else {
            cartItem = CartItem.create(
                    cart,
                    product,
                    request.quantity()
            );

            cartItemRepository.save(cartItem);
        }

        return CartItemResponse.from(cartItem);
    }
}