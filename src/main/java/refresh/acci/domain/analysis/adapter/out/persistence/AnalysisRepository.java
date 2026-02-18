package refresh.acci.domain.analysis.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Page<Analysis> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}