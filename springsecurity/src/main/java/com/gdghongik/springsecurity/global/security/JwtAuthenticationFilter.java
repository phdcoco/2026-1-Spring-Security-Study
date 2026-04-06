package com.gdghongik.springsecurity.global.security;

import com.gdghongik.springsecurity.domain.member.entity.MemberRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 커스텀 토큰 인증 필터, jwtProvider 생성자 주입
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    // 얘네들을 받으면 어떤 동작을 할 건지 정의하라.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 인증 정보를 가공한 뒤 진짜 JWT 토큰만을 가져 온다.
        String token = resolveToken(request);

        // 진위 판별
        if (token != null && jwtProvider.isValid(token)) {
            // 속성들을 가져옴
            Claims claims = jwtProvider.getClaims(token);

            // String으로 저장했었으니까 다시 long으로 변환
            long memberId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            // Enum 타입이니까 valueOf를 이용해준다.
            MemberRole role = MemberRole.valueOf(claims.get("role", String.class));

            // JWT는 Stateless 하니까 password는 null로 처리.
            CustomUserDetails userDetails = new CustomUserDetails(memberId, username, null, role);
            // 이 요청은 인증된 사용자라고 Spring Security에 등록한다.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // request에서 토큰을 가져 온다.
    private String resolveToken(HttpServletRequest request) {
        // Authorization 헤더에서 가져 온다.
        String bearer = request.getHeader("Authorization");
        // 이 bearer String을 바로 토큰으로 변환하는 게 아닌 필요한 정보들만 골라 변환한다.
        if (bearer != null && bearer.startsWith("Bearer ")) {
            // "Bearer " 로 시작하니까 7글자 뺀다.
            return bearer.substring(7);
        }
        return null;
    }
}
