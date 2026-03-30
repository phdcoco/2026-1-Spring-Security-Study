package com.gdghongik.springsecurity.global.security;

import com.gdghongik.springsecurity.domain.member.entity.Member;
import com.gdghongik.springsecurity.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // DB에 유저가 존재하는지 확인, 있어야 인증해줌
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new UsernameNotFoundException(username);
        }

        return CustomUserDetails.from(member);
    }
}
