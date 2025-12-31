package refresh.acci.domain.auth.infra;

import refresh.acci.domain.auth.model.AuthCode;

import java.util.Optional;

public interface AuthCodeRepository {

    void save(AuthCode authCode);

    Optional<AuthCode> findByCode(String code);

    void deleteByCode(String code);
}
