package refresh.acci.global.security.oauth.strategy;

import org.springframework.stereotype.Component;
import refresh.acci.global.security.oauth.attributes.KakaoOAuthAttributes;
import refresh.acci.global.security.oauth.attributes.OAuthAttributes;

import java.util.Map;

@Component
public class KakaoOAuthResponseStrategy implements OAuthResponseStrategy{

    @Override
    public String getProviderName() {
        return "kakao";
    }

    @Override
    public OAuthAttributes createOAuthAttributes(Map<String, Object> attributes) {
        return KakaoOAuthAttributes.of(attributes);
    }
}
