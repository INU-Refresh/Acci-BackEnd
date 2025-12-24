package refresh.acci.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.user.infra.UserRepository;
import refresh.acci.domain.user.model.User;
import refresh.acci.domain.user.presentation.dto.MyPageResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

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

    /*
    회의 후 softDelete 및 정보 보관 기간 설정
    @Transactional
    public void deleteAccount(Long userId, HttpServletResponse response) {
        User user = findUserById(userId);
        String providerId = user.getProviderId();
        userRepository.deleteById(userId);
        CookieUtil.deleteRefreshTokenCookie(response);
        log.info("회원 탈퇴 완료 - userId: {}, providerId: {}", userId, providerId);
    }
     */

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }


}
