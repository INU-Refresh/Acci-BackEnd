package refresh.acci.global.security.oauth.attributes;

import refresh.acci.domain.user.model.User;

public interface OAuthAttributes {
    String getProvider();
    String getProviderId();
    String getName();
    String getEmail();
    String getProfileImage();
    User toEntity();
}
