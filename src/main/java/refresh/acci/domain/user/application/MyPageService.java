package refresh.acci.domain.user.application;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.user.infra.UserRepository;
import refresh.acci.domain.user.model.User;
import refresh.acci.domain.user.presentation.dto.MyPageResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import refresh.acci.global.util.CookieUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageResponse getMyPage(Long userId) {
        User user = findUserById(userId);
        return MyPageResponse.of(user);
    }


    @Transactional
    public void deleteAccount(Long userId, HttpServletResponse response) {
        User user = findUserById(userId);
        String providerId = user.getProviderId();

        user.softDelete();

        CookieUtil.deleteRefreshTokenCookie(response);
        log.info("회원 탈퇴 완료 - userId: {}, providerId: {}", userId, providerId);
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }


}
