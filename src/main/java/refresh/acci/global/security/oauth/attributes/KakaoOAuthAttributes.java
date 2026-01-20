package refresh.acci.global.security.oauth.attributes;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.enums.Role;
import refresh.acci.domain.user.model.User;

import java.util.Map;

@Getter
@Builder
public class KakaoOAuthAttributes implements OAuthAttributes {
    private String provider;
    private String providerId;
    private String name;
    private String email;
    private String profileImage;

    public static KakaoOAuthAttributes of(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoOAuthAttributes.builder()
                .provider("kakao")
                .providerId(String.valueOf(attributes.get("id")))
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .profileImage((String) profile.get("profile_image_url"))
                .build();
    }

    @Override
    public User toEntity() {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .email(email)
                .profileImage(profileImage)
                .role(Role.USER)
                .build();
    }
}
