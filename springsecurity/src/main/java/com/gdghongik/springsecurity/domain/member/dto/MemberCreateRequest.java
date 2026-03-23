package com.gdghongik.springsecurity.domain.member.dto;

public record MemberCreateRequest(
        String username,
        String password
) {}
