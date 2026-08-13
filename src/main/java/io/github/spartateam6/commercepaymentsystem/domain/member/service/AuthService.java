package io.github.spartateam6.commercepaymentsystem.domain.member.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginResponse;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.SignUpRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import io.github.spartateam6.commercepaymentsystem.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();
        memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.createToken(member.getId());
        return new LoginResponse(accessToken);
    }
}
