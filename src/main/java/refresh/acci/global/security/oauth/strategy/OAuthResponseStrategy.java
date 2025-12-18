package refresh.acci.global.security.oauth.strategy;

import refresh.acci.global.security.oauth.attributes.OAuthAttributes;

import java.util.Map;

public interface OAuthResponseStrategy {
    //OAuth 제공자 이름 반환
    String getProviderName();

    //OAuth 제공자로부터 받은 사용자 정보를 가공하여 OAuthAttributes로 변환
    OAuthAttributes createOAuthAttributes(Map<String, Object> attributes);
}
