package refresh.acci.domain.auth.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuthCode {

    private static final int DEFAULT_VALIDITY_SECONDS = 30;

    private final String code;
    private final String accessToken;
    private final Long accessTokenExpiresIn;
    private final String refreshToken;
    private final Long refreshTokenExpiresIn;
    private final LocalDateTime expiresAt;

    public AuthCode(String code, String accessToken, Long accessTokenExpiresIn, String refreshToken, Long refreshTokenExpiresIn, int validitySeconds) {
        this.code = code;
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.expiresAt = LocalDateTime.now().plusSeconds(validitySeconds);
    }

    public static AuthCode of(String code, String accessToken, Long accessTokenExpiresIn, String refreshToken, Long refreshTokenExpiresIn) {
        return new AuthCode(code, accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn, DEFAULT_VALIDITY_SECONDS);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
