package com.gdghongik.springsecurity.global.config;

import com.gdghongik.springsecurity.global.security.JwtAuthenticationFilter;
import com.gdghongik.springsecurity.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 기본 폼 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                // csrf는 켜 주는 게 좋지만, 테스트 환경에서는 disable
                .csrf(csrf -> csrf.disable())
                // 세션 정의
                .sessionManagement(session -> session
                        // JWT에서는 STATELESS
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 멤버의 권한 정의
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login").permitAll()
                        .requestMatchers("/crud/members/**").hasAnyRole("REGULAR")
                        .anyRequest().authenticated())
                /*
                   JWT는 SpringSecurity에서 알아서 안 해주므로 인증 로직을 짜 줘야 함.
                   JwtAuthenticationFilter 클래스 이용.
                   JWT 필터를 UsernamePasswordAuthenticationFilter보다 먼저 실행하겠다. 이건 기본 로그인 필터라 사실상 아무 동작도 안 함.
                   로그인 전 JWT 필터 실행.
                */
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


