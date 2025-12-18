package refresh.acci.global.security.oauth.attributes;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.Role;
import refresh.acci.domain.user.model.User;

import java.util.Map;

@Getter
@Builder
public class KakaoOAuthAttributes implements OAuthAttributes {
    private String name;
    private String email;
    private String profileImage;

    public static KakaoOAuthAttributes of(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoOAuthAttributes.builder()
                .name((String) profile.get("name"))
                .email((String) profile.get("email"))
                .profileImage((String) profile.get("profile_image"))
                .build();
    }

    @Override
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .profileImage(profileImage)
                .role(Role.USER)
                .build();
    }
}
