package refresh.acci.domain.auth.application;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.auth.presentation.dto.TokenResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import refresh.acci.global.security.jwt.JwtTokenProvider;
import refresh.acci.global.security.jwt.TokenDto;
import refresh.acci.global.util.CookieUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse refresh(String refreshToken) {
        validateToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        return TokenResponse.from(tokenDto);
    }

    public void logout(String providerId, HttpServletResponse response) {
        CookieUtil.deleteRefreshTokenCookie(response);
        log.info("로그아웃 완료 - providerId: {}", providerId);
    }


    //토큰 유효성 검사
    private void validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }


}
