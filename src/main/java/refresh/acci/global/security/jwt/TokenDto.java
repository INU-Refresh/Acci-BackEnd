package refresh.acci.global.security.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {
    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
    private final Long accessTokenExpiresAt;
    private final Long refreshTokenExpiresAt;
    private final Integer accessTokenMaxAge;
    private final Integer refreshTokenMaxAge;
}
