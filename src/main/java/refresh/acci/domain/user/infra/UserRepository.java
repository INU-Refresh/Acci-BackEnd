package refresh.acci.domain.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.user.model.Provider;
import refresh.acci.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // === 활성 사용자 조회 (deleted = false) ===
    Optional<User> findByIdAndDeletedFalse(Long id);
    Optional<User> findByProviderIdAndDeletedFalse(String providerId);
    Optional<User> findByProviderAndProviderIdAndDeletedFalse(Provider provider, String providerId);

    // === 전체 사용자 조회 (탈퇴 포함) ===
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    // === 물리 삭제용 ===
    List<User> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoffDate);

}
