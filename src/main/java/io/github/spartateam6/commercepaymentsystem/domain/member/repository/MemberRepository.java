package io.github.spartateam6.commercepaymentsystem.domain.member.repository;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
}
