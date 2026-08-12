package io.github.spartateam6.commercepaymentsystem.domain.member.dto;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {

    private final String email;
    private final String name;
    private final String phoneNumber;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                 member.getEmail(), member.getName(), member.getPhoneNumber()
        );
    }
}
