package refresh.acci.domain.law.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import refresh.acci.domain.law.model.Law;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface LawRepository extends JpaRepository<Law, Long> {

    @Query("""
    SELECT new refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse.RelatedLawsResponse(
        l.name,
        l.content
    )
    FROM Law l
    WHERE l.analysisId = :analysisId
""")
    List<RagSummaryResponse.RelatedLawsResponse> findAllLawsByAnalysisId(@Param("analysisId") UUID analysisId);

    void deleteByAnalysisId(UUID analysisId);
}
