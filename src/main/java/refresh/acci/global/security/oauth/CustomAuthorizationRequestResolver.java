package refresh.acci.global.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import refresh.acci.global.config.OAuthProperties;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final Set<String> allowedOrigins;
    private final String defaultOrigin;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, OAuthProperties oAuthProperties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                AUTHORIZATION_REQUEST_BASE_URI
        );
        this.allowedOrigins = new HashSet<>(oAuthProperties.getAllowedOrigins());
        this.defaultOrigin = oAuthProperties.getDefaultOrigin();

        log.info("OAuth 허용된 Origins: {}", allowedOrigins);
        log.info("OAuth 기본 Origin: {}", defaultOrigin);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {

        if (authorizationRequest == null) { return null; }

        //Origin 추출 (Referer 또는 Origin 헤더 사용)
        String origin = extractOrigin(request);

        //허용된 origin인지 검증
        String validatedOrigin = validateOrigin(origin);

        //State에 origin 정보 인코딩
        String customState = encodeState(authorizationRequest.getState(), validatedOrigin);

        //추가 파라미터 설정
        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("state", customState);

        log.info("OAuth 요청 origin: {}, encoded state 생성", validatedOrigin);

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(customState)
                .additionalParameters(additionalParameters)
                .build();
    }

    //Origin 추출
    private String extractOrigin(HttpServletRequest request) {
        //Origin 헤더 확인
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }

        //Referer 헤더에서 추출
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                java.net.URL url = new java.net.URL(referer);
                return url.getProtocol() + "://" + url.getAuthority();
            } catch (MalformedURLException e) {
                log.warn("Referer URL 형식 오류: {}", referer);
            }
        }

        log.warn("Origin/Referer 헤더를 찾을 수 없음, 기본값 사용");
        return defaultOrigin;
    }

    //Origin 검증
    private String validateOrigin(String origin) {
        if (origin == null || !allowedOrigins.contains(origin)) {
            log.warn("허용되지 않은 origin: {}, 기본값으로 대체", origin);
            return defaultOrigin;
        }
        return origin;
    }

    //State 인코딩: originalState|origin
    private String encodeState(String originalState, String origin) {
        String combined = originalState + "|" + origin;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }
}