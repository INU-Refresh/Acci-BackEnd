package refresh.acci.global.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import refresh.acci.global.config.CookieProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtil {

    private static final String ACCESS_TOKEN_NAME = "accessToken";
    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    private final CookieProperties cookieProperties;


    public void setAccessTokenCookie(HttpServletResponse response, String accessToken, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(ACCESS_TOKEN_NAME, accessToken)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieProperties.getAccessTokenSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isEmpty()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieProperties.getRefreshTokenSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isEmpty()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(cookieProperties.getAccessTokenSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isEmpty()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(cookieProperties.getRefreshTokenSameSite());

        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isEmpty()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void setAuthTokenCookies(HttpServletResponse response, String accessToken, Integer accessMaxAge, String refreshToken, Integer refreshMaxAge) {
        setAccessTokenCookie(response, accessToken, accessMaxAge);
        setRefreshTokenCookie(response, refreshToken, refreshMaxAge);
        log.debug("인증 쿠키 설정 완료 - AccessToken, RefreshToken");
    }

    //로그아웃: Access Token + Refresh Token 모두 삭제
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
        log.debug("인증 쿠키 삭제 완료 - AccessToken, RefreshToken");
    }
}
