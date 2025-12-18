package refresh.acci.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";

    private final String key;
    private final long accessTokenValidityInMilliSeconds;
    private final long refreshTokenValidityInMilliSeconds;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.key}") String key,
            @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityInMilliSeconds,
            @Value("${jwt.refresh-token-validity-in-milliseconds}") long refreshTokenValidityInMilliseconds,
            UserDetailsService userDetailsService) {
        this.key = key;
        this.accessTokenValidityInMilliSeconds = accessTokenValidityInMilliSeconds;
        this.refreshTokenValidityInMilliSeconds = refreshTokenValidityInMilliseconds;
        this.userDetailsService = userDetailsService;
    }

    //AccessToken, RefreshToken 생성
    public TokenDto generateTokenDto(Authentication authentication) {
        String authorities = extractAuthorities(authentication);
        long now = new Date().getTime();

        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliSeconds);
        Date refreshTokenExpiresIn = new Date(now + refreshTokenValidityInMilliSeconds);

        String accessToken = createAccessToken(
                authentication.getName(),
                authorities,
                accessTokenExpiresIn
        );
        String refreshToken = createRefreshToken(refreshTokenExpiresIn);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    //Jwt Token에서 인증 정보 추출
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        if (!claims.containsKey(AUTHORITIES_KEY)) {
            throw new RuntimeException("권한정보가 없는 토큰입니다."); //추후 CustomException으로 변경
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    //토큰 유효성 검사 (추후 CustomException으로 변경)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT Token입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    //Token에서 사용자 Email 추출
    public String getUserEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }


    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    //권한 추출
    private String extractAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    //AccessToken 생성
    private String createAccessToken(String subject, String authorities, Date expiresIn) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(expiresIn)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    //RefreshToken 생성
    private String createRefreshToken(Date expiresIn) {
        return Jwts.builder()
                .setExpiration(expiresIn)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    //Jwt Token 파싱
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
