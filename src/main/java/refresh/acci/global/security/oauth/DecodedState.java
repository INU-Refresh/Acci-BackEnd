package refresh.acci.global.security.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DecodedState {
    private final String originalState;
    private final String origin;
}
