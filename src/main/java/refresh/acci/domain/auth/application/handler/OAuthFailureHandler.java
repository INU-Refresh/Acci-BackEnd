package refresh.acci.domain.auth.application.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import refresh.acci.global.security.oauth.DecodedState;
import refresh.acci.global.security.oauth.StateDecoder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final StateDecoder stateDecoder;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        String errorMessage = exception.getLocalizedMessage();

        //State에서 origin 추출
        String state = request.getParameter("state");
        DecodedState decodedState = stateDecoder.decode(state);
        String origin = decodedState.getOrigin();

        //Origin별 redirect URI 생성
        String redirectUri = origin + "/oauth2/redirect";

        log.warn("OAuth 로그인 실패 - origin: {}, error: {}", origin, errorMessage);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "oauth_failed")
                .queryParam("message", errorMessage)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
