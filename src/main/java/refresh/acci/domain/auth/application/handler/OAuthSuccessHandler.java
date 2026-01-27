package refresh.acci.domain.auth.application.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import refresh.acci.domain.auth.infra.AuthCodeRepository;
import refresh.acci.domain.auth.model.AuthCode;
import refresh.acci.global.security.jwt.JwtTokenProvider;
import refresh.acci.global.security.jwt.TokenDto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCodeRepository authCodeRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://acci-ai.vercel.app"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        //Access Token + Refresh Token을 AuthCode에 저장 (30초 유효)
        String code = UUID.randomUUID().toString();
        AuthCode authCode = AuthCode.of(
                code,
                tokenDto.getAccessToken(),
                tokenDto.getAccessTokenExpiresIn(),
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpiresIn()
        );
        authCodeRepository.save(authCode);

        log.info("OAuth 로그인 성공 - providerId: {}", authentication.getName());
        log.debug("AuthCode 발급: {}", code.substring(0, 8) + "...");

        //프론트엔드 리다이렉트 URL 결정 (Origin 헤더 우선)
        String frontendRedirectUrl = determineFrontendRedirectUri(request);

        //AuthCode를 쿼리 파라미터로 FE에 전달
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("code", code)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    //Front Redirect URI 결정
    private String determineFrontendRedirectUri(HttpServletRequest request) {
        String origin = request.getHeader("Origin");

        //Origin 헤더 검증
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            log.debug("Origin 헤더에서 리다이렉트 URI 결정: {}", origin);
            return buildRedirectUrl(origin);
        }

        //Origin이 없거나 허용되지 않은 경우 기본값 사용
        if (origin != null) {
            log.warn("허용되지 않은 Origin: {}, 기본 redirect URI 사용", origin);
        } else {
            log.debug("Origin 헤더 없음, 기본 redirect URI 사용");
        }

        return redirectUri;
    }

    //Redirect URL 생성
    private String buildRedirectUrl(String origin) {
        return origin + extractRedirectPath();
    }

    //Path 추출
    private String extractRedirectPath() {
        try {
            return new URI(redirectUri).getPath();
        } catch (URISyntaxException e) {
            log.warn("잘못된 redirect URI 형식: {}, 기본값 사용", redirectUri);
            return "/oauth2/redirect";
        }
    }

}