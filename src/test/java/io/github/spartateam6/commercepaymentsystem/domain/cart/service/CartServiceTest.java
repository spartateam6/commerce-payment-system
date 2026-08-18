package io.github.spartateam6.commercepaymentsystem.domain.cart.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemQuantityUpdateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.Cart;
import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import io.github.spartateam6.commercepaymentsystem.domain.cart.repository.CartRepository;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void updateItemQuantity_성공하면_최종수량과_총액을_반환한다() {
        Long memberId = 1L;
        Long cartItemId = 10L;
        CartItemQuantityUpdateRequest request =
                new CartItemQuantityUpdateRequest(3);
        Product product = mock(Product.class);
        CartItem cartItem = mock(CartItem.class);

        given(cartItemRepository.findByIdAndCart_Member_Id(cartItemId, memberId))
                .willReturn(Optional.of(cartItem));
        given(cartItem.getProduct()).willReturn(product);
        given(product.getStock()).willReturn(5);
        given(cartItem.getId()).willReturn(cartItemId);
        given(product.getId()).willReturn(20L);
        given(product.getName()).willReturn("상품");
        given(product.getPrice()).willReturn(10_000);
        given(cartItem.getQuantity()).willReturn(3);

        CartItemResponse response = cartService.updateItemQuantity(
                memberId,
                cartItemId,
                request
        );

        verify(cartItem).changeQuantity(3);
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.totalPrice()).isEqualTo(30_000);
    }

    @Test
    void updateItemQuantity_수량이_0이하면_예외를_던진다() {
        CartItemQuantityUpdateRequest request =
                new CartItemQuantityUpdateRequest(0);

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_QUANTITY.getMessage());

        verify(cartItemRepository, never())
                .findByIdAndCart_Member_Id(10L, 1L);
    }

    @Test
    void updateItemQuantity_본인_항목이_없으면_예외를_던진다() {
        given(cartItemRepository.findByIdAndCart_Member_Id(10L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity(
                1L,
                10L,
                new CartItemQuantityUpdateRequest(1)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CART_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void updateItemQuantity_재고를_초과하면_예외를_던진다() {
        Product product = mock(Product.class);
        CartItem cartItem = mock(CartItem.class);

        given(cartItemRepository.findByIdAndCart_Member_Id(10L, 1L))
                .willReturn(Optional.of(cartItem));
        given(cartItem.getProduct()).willReturn(product);
        given(product.getStock()).willReturn(2);

        assertThatThrownBy(() -> cartService.updateItemQuantity(
                1L,
                10L,
                new CartItemQuantityUpdateRequest(3)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_STOCK.getMessage());

        verify(cartItem, never()).changeQuantity(3);
    }

    @Test
    void deleteItem_본인_장바구니_항목이면_삭제한다() {
        Member member = mock(Member.class);
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);

        given(cartItemRepository.findById(10L))
                .willReturn(Optional.of(cartItem));
        given(cartItem.getCart()).willReturn(cart);
        given(cart.getMember()).willReturn(member);
        given(member.getId()).willReturn(1L);

        cartService.deleteItem(1L, 10L);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void deleteItem_항목이_없으면_예외를_던진다() {
        given(cartItemRepository.findById(10L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteItem(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CART_ITEM_NOT_FOUND.getMessage());

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void deleteItem_다른_회원의_항목이면_예외를_던진다() {
        Member member = mock(Member.class);
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);

        given(cartItemRepository.findById(10L))
                .willReturn(Optional.of(cartItem));
        given(cartItem.getCart()).willReturn(cart);
        given(cart.getMember()).willReturn(member);
        given(member.getId()).willReturn(2L);

        assertThatThrownBy(() -> cartService.deleteItem(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_ACCESS.getMessage());

        verify(cartItemRepository, never()).delete(cartItem);
    }

    @Test
    void clearCart_장바구니가_있으면_모든_항목을_삭제한다() {
        Cart cart = mock(Cart.class);

        given(cartRepository.findByMember_Id(1L))
                .willReturn(Optional.of(cart));
        given(cart.getId()).willReturn(10L);

        cartService.clearCart(1L);

        verify(cartItemRepository).deleteAllByCart_Id(10L);
    }

    @Test
    void clearCart_장바구니가_없어도_정상_처리한다() {
        given(cartRepository.findByMember_Id(1L))
                .willReturn(Optional.empty());

        cartService.clearCart(1L);

        verify(cartItemRepository, never()).deleteAllByCart_Id(any());
    }
}
