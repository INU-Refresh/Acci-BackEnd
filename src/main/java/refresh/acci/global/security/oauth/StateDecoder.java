package refresh.acci.global.security.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.global.config.OAuthProperties;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateDecoder {

    private final OAuthProperties oAuthProperties;

    public DecodedState decode(String encodedState) {
        if (encodedState == null || encodedState.isBlank()) {
            log.warn("State가 비어있음, 기본값 사용");
            return new DecodedState("", oAuthProperties.getDefaultOrigin());
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encodedState));
            String[] parts = decoded.split("\\|", 2);

            if (parts.length == 2) {
                log.debug("State 디코딩 성공 - origin: {}", parts[1]);
                return new DecodedState(parts[0], parts[1]);
            } else {
                log.warn("State 형식이 올바르지 않음, 기본값 사용");
                return new DecodedState(decoded, oAuthProperties.getDefaultOrigin());
            }
        } catch (IllegalArgumentException e) {
            log.warn("State 디코딩 실패 (잘못된 Base64): {}", e.getMessage());
            return new DecodedState("", oAuthProperties.getDefaultOrigin());
        } catch (Exception e) {
            log.error("State 디코딩 중 예상치 못한 오류", e);
            return new DecodedState("", oAuthProperties.getDefaultOrigin());
        }
    }
}