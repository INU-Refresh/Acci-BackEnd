package refresh.acci.global.security.oauth.strategy;

import org.springframework.stereotype.Component;
import refresh.acci.global.security.oauth.attributes.NaverOAuthAttributes;
import refresh.acci.global.security.oauth.attributes.OAuthAttributes;

import java.util.Map;

@Component
public class NaverOAuthResponseStrategy implements OAuthResponseStrategy {

    @Override
    public String getProviderName() {
        return "naver";
    }

    @Override
    public OAuthAttributes createOAuthAttributes(Map<String, Object> attributes) {
        return NaverOAuthAttributes.of(attributes);
    }
}
