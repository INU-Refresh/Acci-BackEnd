package refresh.acci.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.User;

@Getter
public class MyPageResponse {
    private final String name;
    private final String email;
    private final String profileImage;
    private final String role;

    @Builder
    public MyPageResponse(final String name, final String email, final String profileImage, final String role) {
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.role = role;
    }

    public static MyPageResponse of(User user) {
        return MyPageResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }
}
