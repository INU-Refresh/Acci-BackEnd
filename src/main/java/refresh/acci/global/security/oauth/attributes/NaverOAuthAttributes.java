package refresh.acci.global.security.oauth.attributes;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.enums.Role;
import refresh.acci.domain.user.model.User;

import java.util.Map;

@Getter
@Builder
public class NaverOAuthAttributes implements OAuthAttributes {
    private String provider;
    private String providerId;
    private String name;
    private String email;
    private String profileImage;

    public static NaverOAuthAttributes of(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return NaverOAuthAttributes.builder()
                .provider("naver")
                .providerId((String) response.get("id"))
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .profileImage((String) response.get("profile_image"))
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
