package refresh.acci.domain.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.user.model.Provider;
import refresh.acci.domain.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
