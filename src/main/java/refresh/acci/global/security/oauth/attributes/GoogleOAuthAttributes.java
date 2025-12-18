package refresh.acci.global.security.oauth.attributes;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.Role;
import refresh.acci.domain.user.model.User;

import java.util.Map;

@Getter
@Builder
public class GoogleOAuthAttributes implements OAuthAttributes {
    private String name;
    private String email;
    private String profileImage;

    public static GoogleOAuthAttributes of(Map<String, Object> attributes) {
        return GoogleOAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profileImage((String) attributes.get("profileImage"))
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
