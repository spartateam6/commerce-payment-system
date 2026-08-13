package io.github.spartateam6.commercepaymentsystem.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final String accessToken;
}
