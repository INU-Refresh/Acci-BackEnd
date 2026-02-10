package refresh.acci.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuthProperties {
    @NotEmpty(message = "허용된 origin 목록은 최소 1개 이상이어야 합니다")
    private final List<String> allowedOrigins;

    @NotBlank(message = "기본 origin은 필수입니다")
    @Pattern(regexp = "^https?://.*", message = "origin은 http:// 또는 https://로 시작해야 합니다")
    private final String defaultOrigin;
}
