package com.gdghongik.springsecurity.domain.member.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    // ROLE 접두사를 붙인다.
    REGULAR("ROLE_REGULAR"),
    ADMIN("ROLE_ADMIN");

    private final String value;
}
