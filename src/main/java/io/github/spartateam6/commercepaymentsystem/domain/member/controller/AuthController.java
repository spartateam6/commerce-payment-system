package io.github.spartateam6.commercepaymentsystem.domain.member.controller;

import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.LoginResponse;
import io.github.spartateam6.commercepaymentsystem.domain.member.dto.SignUpRequest;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.AuthService;
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

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
