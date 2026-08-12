package io.github.spartateam6.commercepaymentsystem.domain.member.controller;

import io.github.spartateam6.commercepaymentsystem.domain.member.dto.SignUpRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }
}
