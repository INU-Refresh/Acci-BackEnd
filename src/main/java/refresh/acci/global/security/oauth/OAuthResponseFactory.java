package refresh.acci.global.security.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.global.security.oauth.attributes.OAuthAttributes;
import refresh.acci.global.security.oauth.strategy.OAuthResponseStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthResponseFactory {

    private final Map<String, OAuthResponseStrategy> strategies;

    //Spring이 OAuthResponseStrategy 구현체들을 자동으로 주입
    public OAuthResponseFactory(List<OAuthResponseStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        OAuthResponseStrategy::getProviderName,
                        Function.identity(),
                        (existing, replacement) -> {
                            throw new IllegalStateException("Duplicate OAuth provider: " + existing.getProviderName());
                        }
                ));
        log.info("OAuth 제공자: {}", strategies.keySet());
    }

    //제공자에 맞는 OAuthAttributes 생성
    public OAuthAttributes createOAuthAttributes(String provider, Map<String, Object> attributes) {
        OAuthResponseStrategy strategy = strategies.get(provider.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        }
        return strategy.createOAuthAttributes(attributes);
    }
}
