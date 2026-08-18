package io.github.spartateam6.commercepaymentsystem.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public record SignUpRequest(

        @NotNull
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다"
        )
        String password,

        @NotBlank
        String name,

        @NotBlank
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$",
                message = "전화번호는 010-0000-0000 형식이어야 합니다")
        String phoneNumber
) {
}
