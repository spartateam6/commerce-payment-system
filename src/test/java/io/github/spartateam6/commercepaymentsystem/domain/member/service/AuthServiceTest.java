package io.github.spartateam6.commercepaymentsystem.domain.member.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginResponse;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.SignUpRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import io.github.spartateam6.commercepaymentsystem.global.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void signUp_성공하면_암호화된_비밀번호로_저장한다() {

        SignUpRequest request = new SignUpRequest(
                "trexs1004@gmail.com",
                "qwer1234@",
                "장준혁",
                "010-1111-1111"
        );
        // 중복되지 않은 상황 설정
        given(memberRepository.existsByEmail(request.email()))
                .willReturn(false);
        // 비밀번호 암호화 한 결과 설정
        given(passwordEncoder.encode(request.password()))
                .willReturn("encodedPassword");
        //when
        authService.signUp(request);

        //then
        //비밀번호 암호화 호출 검증
        verify(passwordEncoder).encode(request.password());

        //Repository에 저장된 Member 객체르 가져오기 위 해 인자 캡처
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepository).save(captor.capture());

        Member savedMember = captor.getValue();

        // 암호화된 비밀번호가 Member에 저장됐는지 검증
        assertThat(savedMember.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void signUp_이메일이_중복이면_예외를_던진다() {

        //given
        SignUpRequest request = new SignUpRequest(
                "trexs1004@gmail.com",
                "qwer1234@",
                "장준혁",
                "010-1111-1111"
        );
        // 이미 존재하는 이메일 설정
        given(memberRepository.existsByEmail(request.email()))
                .willReturn(true);

        //when & then
        // 중복 이메일이면 예외발생 검증
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

        // 중복 이메일이면 회원 저장이 호출됐는지 검증
        verify(memberRepository, never()).save(any());
    }

    @Test
    void Login_성공하면_토큰_발급한다() {
        // given
        LoginRequest request = new LoginRequest(
                "trexs1004@gmail.com",
                "qwer1234@"
        );
        Member member = Member.builder()
                .id(1L)
                .email("trexs1004@gmail.com")
                .password("encoded-password")
                .build();

        // 이메일로 회원 조회 설정
        given(memberRepository.findByEmail(request.email()))
                .willReturn(Optional.of(member));

        // 비밀번호 일치 상황 설정
        given(passwordEncoder.matches(request.password(), member.getPassword()))
                .willReturn(true);

        // JWT 생성결과 설정
        given(jwtProvider.createToken(member.getId())).willReturn("fake-jwt-token");

        // when

        LoginResponse response = authService.login(request);

        //then
        // 비밀번호 검증이 호출됐는지 검증
        verify(passwordEncoder)
                .matches(request.password(), member.getPassword());

        // JWT 생성이 호출됐는지 검증
        verify(jwtProvider).createToken(member.getId());

        //발급된 토큰 검증
        assertThat(response.getAccessToken()).isEqualTo("fake-jwt-token");
    }

    @Test
    void Login_비민번호가_틀리면_예외_던진다() {
        // given
        LoginRequest request = new LoginRequest(
                "trexs1004@gmail.com",
                "qwer1234@"
        );

        Member member = Member.builder()
                .id(1L)
                .email(request.email())
                .password("encoded-password")
                .build();

        // 이메일로 회원조회시 회원 존재상황 설정
        given(memberRepository.findByEmail(request.email()))
                .willReturn(Optional.of(member));

        // 입력한 비밀번호가 저장된 비밀번호와 일치하지 않은 상황 설정
        given(passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )).willReturn(false);

        // when & then
        // 비밀번호가 일치하지 않으면 예외 발생하는지 검증
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());


        // 비밀번호 불일치시 JWT 발급 되지 않아야 한다..
        verify(jwtProvider, never()).createToken(any());
    }

    @Test
    void Login_회원이_없으면_예외_던짐() {

        //given
        LoginRequest request = new LoginRequest(
                "unkown@unkown.com",
                "qwer1234@"
        );

        // 이메일로 회원 조회 했는데 회원 없음;;
        given(memberRepository.findByEmail(request.email()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

        // 없으면 비밀번호 검증 및 JWT 발급호출 X
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtProvider, never()).createToken(any());

    }


}