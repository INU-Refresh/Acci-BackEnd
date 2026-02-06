package refresh.acci.domain.auth.presentation.dto;


import lombok.Builder;
import lombok.Getter;
import refresh.acci.global.security.jwt.TokenDto;

@Getter
@Builder
public class TokenResponse {
    private final String grantType;
    private final Long accessTokenExpiresAt;

    //Refresh Token
    public static TokenResponse from(TokenDto tokenDto) {
        return TokenResponse.builder()
                .grantType(tokenDto.getGrantType())
                .accessTokenExpiresAt(tokenDto.getAccessTokenExpiresAt())
                .build();
    }

    //AuthCode 교환용
    public static TokenResponse from(Long accessTokenExpiresAt) {
        return TokenResponse.builder()
                .grantType("Bearer")
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .build();
    }
}
