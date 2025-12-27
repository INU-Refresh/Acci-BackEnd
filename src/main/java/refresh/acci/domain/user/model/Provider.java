package refresh.acci.domain.user.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum Provider {
    GOOGLE("google", "Google"),
    KAKAO("kakao", "Kakao"),
    NAVER("naver", "Naver");

    private final String registrationId;
    private final String displayName;

    public static Provider from(String registrationId) {
        if (registrationId == null) {
            throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

        for (Provider provider : values()) {
            if (provider.registrationId.equalsIgnoreCase(registrationId)) {
                return provider;
            }
        }
        throw new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
    }
}
