package refresh.acci.domain.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
