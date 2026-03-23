package com.gdghongik.springsecurity.domain.member.dto;

import com.gdghongik.springsecurity.domain.member.entity.Member;

public record MemberInfoResponse(
        Long memberId,
        String username
) {
    public static MemberInfoResponse from(Member member) {
        // 기본 생성자 호출
        return new MemberInfoResponse(
                member.getId(),
                member.getUsername()
        );
    }
}
