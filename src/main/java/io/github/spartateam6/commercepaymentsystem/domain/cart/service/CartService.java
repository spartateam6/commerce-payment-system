package io.github.spartateam6.commercepaymentsystem.domain.cart.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemAddRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemQuantityUpdateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.Cart;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartRepository;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.service.ProductService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberService memberService;
    private final ProductService productService;

    @Transactional
    public CartItemResponse addItem(
            Long memberId,
            CartItemAddRequest request
    ) {
        Member member = memberService.getMember(memberId);

        Product product = productService.getProductEntity(request.productId());

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
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_STOCK
            );
        }

        CartItem cartItem;

        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.increaseQuantity(
                    request.quantity()
            );
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

    @Transactional
    public CartItemResponse updateItemQuantity(
            Long memberId,
            Long cartItemId,
            CartItemQuantityUpdateRequest request
    ) {
        if (request.quantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        CartItem cartItem = getOwnedCartItem(memberId, cartItemId);
        if (request.quantity() > cartItem.getProduct().getStock()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        cartItem.changeQuantity(request.quantity());
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {
        cartItemRepository.delete(getOwnedCartItem(memberId, cartItemId));
    }

    private CartItem getOwnedCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!cartItem.getCart().getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return cartItem;
    }


    @Transactional(readOnly = true)
    public CartResponse getCart(Long memberId) {

       memberService.getMember(memberId);


        Optional<Cart> optionalCart =
                cartRepository.findByMember_Id(memberId);

        if (optionalCart.isEmpty()) {
            return CartResponse.empty();
        }

        Cart cart = optionalCart.get();

        List<CartItemResponse> items =
                cartItemRepository.findAllByCart_Id(cart.getId())
                        .stream()
                        .map(CartItemResponse::from)
                        .toList();

        return CartResponse.of(cart.getId(), items);
    }

    @Transactional
    public void clearCart(Long memberId) {
        Optional<Cart> optionalCart = cartRepository.findByMember_Id(memberId);

        if (optionalCart.isEmpty()) {
            return;
        }

        cartItemRepository.deleteAllByCart_Id(optionalCart.get().getId());
    }

    @Transactional
    public void deletePurchasedItems(Long memberId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }

        cartRepository.findByMember_Id(memberId)
                .ifPresent(cart -> cartItemRepository.deleteAllByCartIdAndIdIn(
                        cart.getId(),
                        cartItemIds
                ));
    }
}
