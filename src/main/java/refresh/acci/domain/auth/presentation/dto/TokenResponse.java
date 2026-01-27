package refresh.acci.domain.auth.presentation.dto;


import lombok.Builder;
import lombok.Getter;
import refresh.acci.global.security.jwt.TokenDto;

@Getter
public class TokenResponse {
    private final String grantType;
    private final Long accessTokenExpiresIn;

    @Builder
    public TokenResponse(String grantType, Long accessTokenExpiresIn) {
        this.grantType = grantType;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    //Refresh Token
    public static TokenResponse from(TokenDto tokenDto) {
        return TokenResponse.builder()
                .grantType(tokenDto.getGrantType())
                .accessTokenExpiresIn(tokenDto.getAccessTokenExpiresIn())
                .build();
    }

    //AuthCode 교환용
    public static TokenResponse from(Long accessTokenExpiresIn) {
        return TokenResponse.builder()
                .grantType("Bearer")
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
