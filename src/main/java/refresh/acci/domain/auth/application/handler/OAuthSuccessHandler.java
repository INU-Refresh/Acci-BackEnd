package refresh.acci.domain.auth.application.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import refresh.acci.domain.auth.infra.AuthCodeRepository;
import refresh.acci.domain.auth.model.AuthCode;
import refresh.acci.global.security.jwt.JwtTokenProvider;
import refresh.acci.global.security.jwt.TokenDto;
import refresh.acci.global.security.oauth.DecodedState;
import refresh.acci.global.security.oauth.StateDecoder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCodeRepository authCodeRepository;
    private final StateDecoder stateDecoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        //Access Token + Refresh Token을 AuthCode에 저장 (30초 유효)
        String code = UUID.randomUUID().toString();
        AuthCode authCode = AuthCode.of(
                code,
                tokenDto.getAccessToken(),
                tokenDto.getAccessTokenExpiresAt(),
                tokenDto.getAccessTokenMaxAge(),
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpiresAt(),
                tokenDto.getRefreshTokenMaxAge()
        );
        authCodeRepository.save(authCode);

        log.info("OAuth 로그인 성공 - providerId: {}", authentication.getName());
        log.debug("AuthCode 발급: {}", code.substring(0, 8) + "...");

        //state에서 origin 추출
        String state = request.getParameter("state");
        DecodedState decodedState = stateDecoder.decode(state);
        String origin = decodedState.getOrigin();

        //Origin별 redirect URI 생성
        String redirectUri = origin + "/oauth2/redirect";

        log.info("OAuth redirect - origin: {}, redirectUri: {}", origin, redirectUri);

        //AuthCode를 쿼리 파라미터로 FE에 전달
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}