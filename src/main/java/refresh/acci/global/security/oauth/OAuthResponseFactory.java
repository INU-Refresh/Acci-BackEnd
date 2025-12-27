package refresh.acci.global.security.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
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
                            throw new CustomException(ErrorCode.DUPLICATE_OAUTH_PROVIDER);
                        }
                ));
        log.info("OAuth 제공자: {}", strategies.keySet());
    }

    //제공자에 맞는 OAuthAttributes 생성
    public OAuthAttributes createOAuthAttributes(String provider, Map<String, Object> attributes) {
        OAuthResponseStrategy strategy = strategies.get(provider.toLowerCase());
        if (strategy == null) {
            throw new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return strategy.createOAuthAttributes(attributes);
    }
}
