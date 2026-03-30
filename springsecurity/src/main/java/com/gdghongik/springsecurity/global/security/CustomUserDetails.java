package com.gdghongik.springsecurity.global.security;

import com.gdghongik.springsecurity.domain.member.entity.Member;
import com.gdghongik.springsecurity.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String username;
    private final String password;
    private final MemberRole role;

    // 생성자
    public static CustomUserDetails from(Member member) {
        return new CustomUserDetails(member.getId(), member.getUsername(), member.getPassword(), member.getRole());
    }

    // 판별 기준, 지금은 MemberRole에 따라 결정.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
