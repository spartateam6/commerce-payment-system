package io.github.spartateam6.commercepaymentsystem.dummy;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;

import java.util.List;

public class MemberFixture {

    public static final List<Member> members = List.of(
            Member.builder()
                    .id(1L)
                    .email("user1@test.com")
                    .password("$2a$12$GeifU8Kqme5IKtQa9aVAN.CWXCRjHNwIlYssY64Hw.VCBpDnu6wky")
                    .name("테스트유저1")
                    .phoneNumber("010-1111-1111")
                    .build(),

            Member.builder()
                    .id(2L)
                    .email("user2@test.com")
                    .password("$2a$12$GeifU8Kqme5IKtQa9aVAN.CWXCRjHNwIlYssY64Hw.VCBpDnu6wky")
                    .name("테스트유저2")
                    .phoneNumber("010-2222-2222")
                    .build()
    );
}
