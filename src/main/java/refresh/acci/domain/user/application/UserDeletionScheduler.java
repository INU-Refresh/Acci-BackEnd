package refresh.acci.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.user.infra.UserRepository;
import refresh.acci.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeletionScheduler {

    private final UserRepository userRepository;

    //보관 기간: 6개월(180일)
    private static final int RETENTION_DAYS = 180;

    /**
     * 6개월 이상 지난 탈퇴 사용자 물리 삭제
     * 매일 새벽 03:00 시행 (Asia/Seoul 시간대)
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteExpiredUsers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        log.info("탈퇴 사용자 삭제 스케줄 시작 - 기준일: {}", cutoffDate);

        List<User> usersToDelete = userRepository.findByDeletedTrueAndDeletedAtBefore(cutoffDate);

        if (usersToDelete.isEmpty()) {
            log.info("삭제할 탈퇴 사용자 없음");
            return;
        }

        int deletedCount = usersToDelete.size();
        userRepository.deleteAll(usersToDelete);

        log.info("탈퇴 사용자 {}명 물리 삭제 완료", deletedCount);
    }
}
