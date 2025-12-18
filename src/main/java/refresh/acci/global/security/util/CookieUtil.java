package refresh.acci.global.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    //쿠키 생성
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

    //쿠키 조회
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    //쿠키 값 조회
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name).map(Cookie::getValue);
    }

    //쿠키 삭제
    public static void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, null, 0);
    }

    //여러 쿠키 삭제
    public static void deleteCookies(HttpServletResponse response, String[] names) {
        for (String name : names) {
            deleteCookie(response, name);
        }
    }

    //Refresh Token 쿠키 생성
    public static void addRefreshCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        addCookie(response, "refreshToken", refreshToken, maxAge);
    }
}
