package refresh.acci.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.User;

@Getter
@Builder
public class UserInfoResponse {
    private final String name;
    private final String email;
    private final String profileImage;
    private final String role;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }
}
