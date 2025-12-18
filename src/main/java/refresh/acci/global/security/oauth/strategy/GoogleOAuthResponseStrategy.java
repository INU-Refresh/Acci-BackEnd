package refresh.acci.global.security.oauth.strategy;

import org.springframework.stereotype.Component;
import refresh.acci.global.security.oauth.attributes.GoogleOAuthAttributes;
import refresh.acci.global.security.oauth.attributes.OAuthAttributes;

import java.util.Map;

@Component
public class GoogleOAuthResponseStrategy implements OAuthResponseStrategy {

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public OAuthAttributes createOAuthAttributes(Map<String, Object> attributes) {
        return GoogleOAuthAttributes.of(attributes);
    }
}
