package refresh.acci.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
    }

    public static void deleteCookies(HttpServletResponse response, String... names) {
        for (String name : names) {
            deleteCookie(response, name);
        }
    }

    public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        addCookie(response, REFRESH_TOKEN_NAME, refreshToken, maxAge);
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_NAME);
    }
}
