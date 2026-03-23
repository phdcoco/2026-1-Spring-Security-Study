package com.gdghongik.springsecurity.domain.member.repository;

import com.gdghongik.springsecurity.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);
    boolean existsByUsername(String username);
}
