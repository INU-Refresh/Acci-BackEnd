package refresh.acci.domain.auth.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuthCode {

    private static final int DEFAULT_VALIDITY_SECONDS = 30;

    private final String code;
    private final String accessToken;
    private final Long accessTokenExpiresIn;
    private final LocalDateTime expiresAt;

    public AuthCode(String code, String accessToken, Long accessTokenExpiresIn, int validitySeconds) {
        this.code = code;
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.expiresAt = LocalDateTime.now().plusSeconds(validitySeconds);
    }

    public static AuthCode of(String code, String accessToken, Long accessTokenExpiresIn) {
        return new AuthCode(code, accessToken, accessTokenExpiresIn, DEFAULT_VALIDITY_SECONDS);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
