# 토큰 기반 인증

토큰 기반 인증은 서버가 사용자의 인증 상태를 저장하지 않는 stateless한 특징이 있다. 서버는 사용자에게 토큰을 발급해준 뒤, 사용자가 요청하면 서버는 사용자가 지니고 있는 토큰을 검사하고 유효하면 들여보내준다.

토큰과 같은 사용자 식별 장치는 형식이 정해져 있어야 하며, 변조하기 어려워야 한다. 따라서 이 두 가지 조건을 만족시키는 JWT 토큰을 사용한다.

JWT는 json이라는 형식을 사용하며, 서명을 통해 변조하기 어렵게 만든다.

### JWT 구조

토큰의 구조는 Header, Payload, Signature 세 개로 나뉘며 각각은 . 으로 이어진다. 각각에 들어가는 대표적인 정보로는

- Header : 알고리즘 종류, 보통 HS256을 쓴다고 한다.
- Payload : 토큰에서 사용할 정보의 조각들인 Claim이 담겨있다. Claim은 key-value 형식으로 이루어진 한 쌍의 정보들을 의미한다. Claim은 총 3가지로 나누어진다. JWT 표준으로 정해진 Registered Claim, 공개용 정보를 위해 URI 포맷으로 저장되는 Public Claim, 서비스 내부에서만 쓰는 사용자 지정 Private Claim이 있다. Base64 인코딩으로, 암호화가 아니기 때문에 민감한 정보를 넣으면 안 된다.
- Signature : Header의 alg에 써져있는 알고리즘을 이용하여 서명한다.

### JWT로 사용자 구분하기

1. 먼저 사용자로부터 토큰을 꺼낸다.
2. 그 토큰을 파싱하여 정보들을 본다.
3. 이 정보들의 변조 여부를 서명을 통하여 확인한다.
4. 변조 확인이 끝나면 토큰의 사용자 정보를 보고 누구인지 식별한다.

### JWT 주의사항

토큰에는 만료 시간이 있다. 따라서 유효기간을 잘 정해야 한다. 유효기간을 너무 길게 하면 공격자가 해킹하기 쉽다. 유효기간을 짧게 해야 공격자가 사용하기도 전에 끊겨버려 효과가 있다. 하지만 유효기간이 너무 짧으면 오히려 사용자도 불편해진다. 따라서 유효기간을 짧게 하되, refresh token을 이용하여 재발급하도록 한다. 이 refresh token도 해킹될 것에 대비하여 refresh token rotation을 채택하고 있다.

위에서도 말했듯이 BASE64는 단순 인코딩, 디코딩이기 때문에 토큰에 민감한 정보를 넣으면 절대 안된다. 물론 서명을 하기 떄문에 변조는 쉽지 않지만, 열람 자체는 매우 쉽기 때문에 주의하여야 한다.

### Login - Flow

![JWTLogin.png](JWTLogin.png)
앞부분은 세션과 비슷하다.

1. 로그인 요청을 authToken으로 변환한다.
2. AuthenticationManager로 authToken을 이용하여 사용자를 인증한다. 실패 시 AuthenticaionException을 던진다.
    1. SecurityFilterChain 안에 JwtAuthenticationFilter라는 커스텀 필터를 만들어 준다.
3. 인증 정보를 토대로 토큰을 발행한다
4. 토큰을 클라이언트에 전달한다.

### JwtAuthenticationFilter

세션은 Spring Security에서 내부적으로 인증해주지만, JWT는 우리가 직접 커스텀 필터를 만들어주어 인증해야 한다. 따라서 커스텀 클래스를 하나 만들어 주어야 하는데, 이 때 OncePerRequestFilter를 꼭 extends 해 줘야 한다. Spring에서 필터를 잘못 쓰면 토큰을 두 번 검증할 수도 있다. 이러면 인증 객체가 덮어씌워져 다른 상태가 발생할 수도 있고 같은 토큰인데 한 번은 정상, 다른 한 번은 토큰 만료로 실패할 수 있다. 따라서 서비스를 잘 이용하고 있다가 갑자기 튕길 수도 있는 것이다. 일을 두 번 하니 당연히 성능도 낭비된다. 로그도 두 번 찍히니까 디버깅하는 개발자도 혼란이 온다. 🙀 SpringFilterChain도 예상치 못한 토큰을 받아 혼란이 올 수 있다.

따라서 필터가 하나의 요청당 한 번만 실행되도록 보장하는 OncePerRequest를 사용하는 것이 표준 관례이다. 이 클래스를 Inheritance하면 DoFilterInternal을 꼭 Override 해 줘야 한다. (안 하면 에러 뜸)

```jsx
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
```

resolveToken 메서드는 HTTP 요청 헤더에서 Authorization을 꺼내고, 이를 String 타입의 bearer 변수에 넣어 준다. JWT는 Bearer 어쩌구 이렇게 담겨 있기 때문에 “Bearer “를 빼 줘야 진짜 JWT가 나오게 된다. 따라서 subString으로 맨 앞 7글자 (공백 포함)을 제거 하여 진짜 JWT 토큰 값을 반환해 준다.

RFC 6750에는 이렇게 나와있다.

```
This specification describes how to use bearer tokens in HTTP
   requests to access OAuth 2.0 protected resources.  Any party in
   possession of a bearer token (a "bearer") can use it to get access to
   the associated resources (without demonstrating possession of a
   cryptographic key).  To prevent misuse, bearer tokens need to be
   protected from disclosure in storage and in transport.
```

왜 Bearer를 쓰는가? “Bearer”는 소유자라는 뜻인데, 이 토큰의 소유자에게 권한을 부여해달라는 의미로 이름을 붙였다고 한다. HTTP 표준에서는 클라이언트가 서버에 인증 정보를 보낼 때 Authorization 헤더를 사용하는데, 여러 인증 방식 중 JWT는 Bearer Token 인증을 사용하기 때문에 Bearer를 붙여 줘야 알아들을 수 있다.

jwtProvider에서 토큰의 진위 여부를 판별하는 isValid 메서드를 만들어 준다.

```jsx
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
```

디버깅을 수월하게 하려면 예외를 각각 나눠줘야 한다. 여기서는 그냥 false로 퉁쳐 버리는데, 편의를 위해 이렇게 진행한다.

### 실행

POST localhost:8080/auth/signup

```json
{
    "username": "ryu",
    "password": "1234"
}
```

200 OK

POST localhost:8080/auth/login

동일한 json으로 진행하면 200 OK가 뜨면서 JWT 토큰을 반환한다. 여기서는

```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJyeXUiLCJyb2xlIjoiUkVHVUxBUiIsImlhdCI6MTc3NTQ2MDk0NiwiZXhwIjoxNzc1NDY0NTQ2fQ.uidLRNp-_nZq_g1hI74bl2k5UIv3Sy-OaXajPvkY2kxvgCkdY4scIbZ5gWWGyeh0r0WFfUhb3-QfzBqIBpv0hA"
}
```

이렇게 떴다. 재미삼아 디코딩해보면

```json
{"alg":"HS512"}{"sub":"1","username":"ryu","role":"REGULAR","iat":1775460946,"exp":1775464546}tMf;vtj38gXlJXWԅAiH@
```

HS512 알고리즘을 사용하고, id는 1, 이름은 ryu, role은 REGULAR(일반 회원)이며 토큰 생성 시각과 만료 시간이 있다.

GET localhost:8080/crud/members/me

Header에 Key - Authorization Value - Bearer JWT 토큰 값

넣고 실행하면

```json
{
    "memberId": 1,
    "username": "ryu"
}
```

정상 작동 한다.