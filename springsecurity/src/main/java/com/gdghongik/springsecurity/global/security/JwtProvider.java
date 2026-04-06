package com.gdghongik.springsecurity.global.security;


// lombok의 value가 아니라 springframework의 value임!
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    // application.yml 에서 값을 가져올 때 @Value 쓴다.
    // lombok의 value는 private final 불변 객체 만드는 어노테이션이고
    // 여기서는 springframework의 value 써야 된다.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(CustomUserDetails userDetails) {
        // 만료를 계산하기 위해 발행 시간 저장
        Date now = new Date();
        // 만료 시간 계산
        Date expiredAt = new Date(now.getTime() + expiration);

        // JWT 생성
        return Jwts.builder()
                // 식별자
                .subject(userDetails.getMemberId().toString())
                // key - value
                .claim("username", userDetails.getUsername())
                .claim("role", userDetails.getRole().name())
                // 언제 발행됐는가?
                .issuedAt(now)
                // 언제 만료되는가?
                .expiration(expiredAt)
                // 서명
                .signWith(getSecretKey())
                // JWT를 완성한다.
                .compact();
    }

    // signWith에서 Key라는 객체를 받고 있으므로 String인 secret을 가공한다.
    private SecretKey getSecretKey() {
        // hmacShaKeyFor라는 메서드를 사용하며, 이 메서드는 바이트를 받으므로 getBytes로 변환 해준다.
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 정보가 제대로 꺼내와지는가?, Claims는 JWT의 속성을 나타내는 형식, 제네릭 타입임.
    public Claims getClaims(String token) {
        // 읽어야 하니까 builder 아닌 파싱 사용
        return Jwts.parser()
                // 우선 위변조 검증
                .verifyWith(getSecretKey())
                .build()
                // 통과하면 파싱 시작
                .parseSignedClaims(token)
                // 페이로드를 꺼내 옴
                .getPayload();
    }

    // 토큰 진위 판별 메서드
    public boolean isValid(String token) {
        try {
            getClaims(token); // 속성을 다 읽을 수 있다.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
