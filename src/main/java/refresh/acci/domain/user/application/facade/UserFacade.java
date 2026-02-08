package refresh.acci.domain.user.application.facade;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.domain.auth.application.AuthService;
import refresh.acci.domain.user.application.service.UserCommandService;
import refresh.acci.domain.user.application.service.UserQueryService;
import refresh.acci.domain.user.model.User;
import refresh.acci.domain.user.presentation.dto.UserInfoResponse;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final AuthService authService;

    /**
     * 사용자 정보 조회
     */
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userQueryService.getUserById(userId);
        return UserInfoResponse.from(user);
    }

    /**
     * 회원 탈퇴
     * 1. User 계정 삭제 (soft delete)
     * 2. 인증 정보 제거 (쿠키 삭제)
     */
    public void deleteUserAccount(Long userId, HttpServletResponse response) {
        userCommandService.deleteAccount(userId);
        authService.clearAuthentication(response);
    }

}
