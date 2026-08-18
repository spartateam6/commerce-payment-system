package io.github.spartateam6.commercepaymentsystem.domain.member.dto;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;


public record MemberResponse (

    String email,
    String name,
    String phoneNumber
){
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getEmail(), member.getName(), member.getPhoneNumber()
        );
    }
}
