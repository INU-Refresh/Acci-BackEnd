package refresh.acci.domain.auth.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import refresh.acci.domain.auth.model.AuthCode;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class InMemoryAuthCodeRepository implements AuthCodeRepository {

    private final Map<String, AuthCode> storage = new ConcurrentHashMap<>();

    @Override
    public void save(AuthCode authCode) {
        storage.put(authCode.getCode(), authCode);
    }

    @Override
    public Optional<AuthCode> findByCode(String code) {
        AuthCode authCode = storage.get(code);

        if (authCode == null) {
            return Optional.empty();
        }

        if (authCode.isExpired()) {
            storage.remove(code);
            return Optional.empty();
        }

        return Optional.of(authCode);
    }

    @Override
    public void deleteByCode(String code) {
        storage.remove(code);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredCodes() {
        int beforeSize = storage.size();
        storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = storage.size();

        if (beforeSize != afterSize) {
            log.info("만료된 인증 코드 {}개 정리 완료", beforeSize - afterSize);
        }
    }
}
