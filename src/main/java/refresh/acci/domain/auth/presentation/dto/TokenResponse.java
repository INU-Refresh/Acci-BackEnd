package refresh.acci.domain.auth.presentation.dto;


import lombok.Builder;
import lombok.Getter;
import refresh.acci.global.security.jwt.TokenDto;

@Getter
public class TokenResponse {
    private final String grantType;
    private final String accessToken;
    private final Long accessTokenExpiresIn;

    @Builder
    public TokenResponse(String grantType, String accessToken, Long accessTokenExpiresIn) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public static TokenResponse from(TokenDto tokenDto) {
        return TokenResponse.builder()
                .grantType(tokenDto.getGrantType())
                .accessToken(tokenDto.getAccessToken())
                .accessTokenExpiresIn(tokenDto.getAccessTokenExpiresIn())
                .build();
    }
}
