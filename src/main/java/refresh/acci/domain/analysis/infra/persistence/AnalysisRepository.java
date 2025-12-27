package refresh.acci.domain.analysis.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    @Query("""
    SELECT new refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse(
        a.id,
        a.userId,
        a.accidentRate,
        a.accidentType,
        a.analysisStatus,
        a.isCompleted
    )
    FROM Analysis a
    WHERE a.id = :id
""")
    Optional<AnalysisResultResponse> findAnalysesResultById(UUID id);
}
