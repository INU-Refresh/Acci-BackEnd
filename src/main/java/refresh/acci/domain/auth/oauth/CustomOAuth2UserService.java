package refresh.acci.domain.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.user.infra.UserRepository;
import refresh.acci.domain.user.model.CustomOAuthUser;
import refresh.acci.domain.user.model.Provider;
import refresh.acci.domain.user.model.User;
import refresh.acci.global.security.oauth.OAuthResponseFactory;
import refresh.acci.global.security.oauth.attributes.OAuthAttributes;

import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthResponseFactory oauthResponseFactory;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthAttributes oauthAttributes = oauthResponseFactory.createOAuthAttributes(registrationId, attributes);

        User user = saveOrUpdate(oauthAttributes);

        return new CustomOAuthUser(user, attributes);
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        Provider provider = Provider.from(attributes.getProvider());

        User user = userRepository.findByProviderAndProviderIdAndDeletedFalse(provider, attributes.getProviderId())
                .map(existingUser -> updateUser(existingUser, attributes))
                .orElseGet(() -> createUser(attributes));

        return userRepository.save(user);
    }

    private User updateUser(User user, OAuthAttributes attributes) {
        user.update(attributes.getName(), attributes.getProfileImage());
        log.info("유저 정보 업데이트: {}", user.getProviderId());
        return user;
    }

    private User createUser(OAuthAttributes attributes) {
        User user = attributes.toEntity();
        log.info("신규 유저 생성: {}", user.getProviderId());
        return user;
    }

}
