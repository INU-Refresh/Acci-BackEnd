package refresh.acci.domain.precedent.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import refresh.acci.domain.precedent.model.Precedent;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface PrecedentRepository extends JpaRepository<Precedent, Long> {

    @Query("""
    SELECT new refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse.PrecedentCasesResponse(
        p.caseName,
        p.summary,
        p.dateOfJudgment
    )
    FROM Precedent p
    WHERE p.analysisId = :analysisId
""")
    List<RagSummaryResponse.PrecedentCasesResponse> findAllPrecedentsByAnalysisId(@Param("analysisId") UUID analysisId);

    void deleteByAnalysisId(UUID analysisId);
}
