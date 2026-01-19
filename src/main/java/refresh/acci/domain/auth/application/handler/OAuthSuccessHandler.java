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
import refresh.acci.global.util.CookieUtil;

import java.io.IOException;
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

    @Value("${jwt.refresh-token-validity-in-milliseconds}")
    private long refreshTokenValidityInMilliseconds;

    //Redirect Uri White List
    private static final List<String> ALLOWED_REDIRECT_URIS = List.of(
            "https://acci-ai.vercel.app/oauth2/redirect",
            "http://localhost:3000/oauth2/redirect",
            "http://localhost:5173/oauth2/redirect"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        //Refresh Token Cookie에 저장
        int maxAge = (int) (refreshTokenValidityInMilliseconds / 1000);
        CookieUtil.addRefreshTokenCookie(response, tokenDto.getRefreshToken(), maxAge);

        //AuthCode 생성
        String code = UUID.randomUUID().toString();
        AuthCode authCode = AuthCode.of(code, tokenDto.getAccessToken(), tokenDto.getAccessTokenExpiresIn());
        authCodeRepository.save(authCode);
        log.info("인증 코드 발급: {} (유효시간: 30초)", code.substring(0, 8) + "...");

        //local 개발을 위한 redirect_url
        String frontendRedirectUrl = determineFrontendRedirectUri(request);

        //AuthCode를 쿼리 파라미터로 FE에 전달
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("code", code)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    //Referer 헤더 기반으로 프런트 Redirect Uri 분기
    private String determineFrontendRedirectUri(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.startsWith("http://localhost:3000")) {
                return validateAndReturn("http://localhost:3000/oauth2/redirect");
            }
            if (referer.startsWith("http://localhost:5173")) {
                return validateAndReturn("http://localhost:5173/oauth2/redirect");
            }
            if (referer.contains("acci-ai.vercel.app")) {
                return validateAndReturn("https://acci-ai.vercel.app/oauth2/redirect");
            }
        }
        return redirectUri;
    }

    private String validateAndReturn(String uri) {
        if (ALLOWED_REDIRECT_URIS.contains(uri)) {
            return uri;
        }
        log.warn("허용되지 않은 redirect URI 시도: {}", uri);
        return redirectUri;
    }
}

