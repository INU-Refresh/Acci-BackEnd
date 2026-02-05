package refresh.acci.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {
    private boolean secure = true;
    private String domain;
    private String accessTokenSameSite = "Lax";
    private String refreshTokenSameSite = "Strict";
}
