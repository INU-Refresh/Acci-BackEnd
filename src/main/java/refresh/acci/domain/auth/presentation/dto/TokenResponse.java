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

    //Refresh Token
    public static TokenResponse from(TokenDto tokenDto) {
        return TokenResponse.builder()
                .grantType(tokenDto.getGrantType())
                .accessToken(tokenDto.getAccessToken())
                .accessTokenExpiresIn(tokenDto.getAccessTokenExpiresIn())
                .build();
    }

    //AuthCode 교환용
    public static TokenResponse from(String accessToken, Long accessTokenExpiresIn) {
        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
