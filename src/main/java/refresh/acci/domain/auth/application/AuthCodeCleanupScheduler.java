package refresh.acci.domain.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import refresh.acci.domain.auth.infra.AuthCodeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCodeCleanupScheduler {

    private final AuthCodeRepository authCodeRepository;

    /**
     * 만료된 인증 코드 정리
     * 매 1분마다 실행
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredCodes() {
        int beforeSize = authCodeRepository.size();
        authCodeRepository.removeExpiredCodes();
        int afterSize = authCodeRepository.size();

        int removedCount = beforeSize - afterSize;
        if (removedCount > 0) {
            log.info("만료된 인증 코드 {}개 정리 완료", removedCount);
        }
    }
}
