package refresh.acci.domain.analysis.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.List;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    List<Analysis> findAllByUserId(Long userId);

    List<Analysis> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
}