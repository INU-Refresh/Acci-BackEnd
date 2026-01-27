package refresh.acci.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final String ACCESS_TOKEN_NAME = "accessToken";
    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    //SameSite=Lax: OAuth 리다이렉트 시 쿠키 유지를 위해 필요
    public static void setAccessTokenCookie(HttpServletResponse response, String accessToken, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None") //실 배포시 "Lax"로 변경
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None") //실 배포시 "Lax"로 변경
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    //SameSite=Strict: CSRF 공격 최소화
    public static void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None") //실 배포시 "Strict"로 변경
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None") //실 배포시 "Strict"로 변경
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void setAuthTokenCookies(HttpServletResponse response, String accessToken, Long accessTokenExpiresIn, String refreshToken, Long refreshTokenExpiresIn) {
        int accessMaxAge = (int) (accessTokenExpiresIn / 1000);
        setAccessTokenCookie(response, accessToken, accessMaxAge);

        int refreshMaxAge = (int) (refreshTokenExpiresIn / 1000);
        setRefreshTokenCookie(response, refreshToken, refreshMaxAge);
    }

    //로그아웃: Access Token + Refresh Token 모두 삭제
    public static void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }
}
