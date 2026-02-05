package refresh.acci.domain.auth.application;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.auth.infra.AuthCodeRepository;
import refresh.acci.domain.auth.model.AuthCode;
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
    private final AuthCodeRepository authCodeRepository;
    private final CookieUtil cookieUtil;

    @Transactional
    public TokenResponse exchangeCodeForToken(String code, HttpServletResponse response) {
        AuthCode authCode = getAuthCodeOrThrow(code);
        authCodeRepository.deleteByCode(code);

        cookieUtil.setAuthTokenCookies(
                response,
                authCode.getAccessToken(),
                authCode.getAccessTokenMaxAge(),
                authCode.getRefreshToken(),
                authCode.getRefreshTokenMaxAge()
        );

        log.info("인증 코드 교환 성공: {}", code.substring(0, 8) + "...");

        return TokenResponse.from(authCode.getAccessTokenExpiresAt());
    }

    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        validateToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        cookieUtil.setAuthTokenCookies(response,
                tokenDto.getAccessToken(),
                tokenDto.getAccessTokenMaxAge(),
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenMaxAge());

        return TokenResponse.from(tokenDto);
    }

    public void logout(String providerId, HttpServletResponse response) {
        cookieUtil.deleteAllAuthCookies(response);
        log.info("로그아웃 완료 - providerId: {}", providerId);
    }

    //인증 정보 제거
    public void clearAuthentication(HttpServletResponse response) {
        cookieUtil.deleteAllAuthCookies(response);
    }

    //AuthCode 가져오기
    private AuthCode getAuthCodeOrThrow(String code) {
        return authCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_AUTH_CODE));
    }

    //토큰 유효성 검사
    private void validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }


}
