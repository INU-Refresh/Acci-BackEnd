package refresh.acci.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {
    @NotNull(message = "cookie.secure 설정은 필수입니다")
    private final boolean secure;

    private final String domain;

    @NotBlank(message = "accessTokenSameSite는 필수입니다")
    @Pattern(
            regexp = "^(Strict|Lax|None)$",
            message = "accessTokenSameSite는 Strict, Lax, None 중 하나여야 합니다"
    )
    private final String accessTokenSameSite;

    @NotBlank(message = "refreshTokenSameSite는 필수입니다")
    @Pattern(
            regexp = "^(Strict|Lax|None)$",
            message = "refreshTokenSameSite는 Strict, Lax, None 중 하나여야 합니다"
    )
    private final String refreshTokenSameSite;
}
