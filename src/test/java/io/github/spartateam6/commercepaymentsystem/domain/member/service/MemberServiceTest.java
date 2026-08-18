package io.github.spartateam6.commercepaymentsystem.domain.member.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.dto.MemberResponse;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
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

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private  MemberService memberService;

    @Test
    void getMyInfo_성공하면_회원정보를_반환한다() {
    // given
        Member member = Member.builder()
                .id(1L)
                .email("trexs1004@gmail.com")
                .password("qwer1234@")
                .name("장준혁")
                .phoneNumber("010-1111-1111")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMyInfo(1L);

        // then

        assertThat(response.email()).isEqualTo("trexs1004@gmail.com");
        assertThat(response.name()).isEqualTo("장준혁");
        assertThat(response.phoneNumber()).isEqualTo("010-1111-1111");
    }

    @Test
    void getMyInfo_회원이_없으면_예외를_던진다() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }




}