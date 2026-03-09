package refresh.acci.domain.analysis.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Page<Analysis> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Analysis a SET a.ragStatus = 'IN_PROGRESS' " +
            "WHERE a.id = :analysisId AND (a.ragStatus = 'NONE' OR a.ragStatus = 'FAILED')")
    int tryMarkRagInProgress(@Param("analysisId") UUID analysisId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Analysis a SET a.ragStatus = 'DONE' " +
            "WHERE a.id = :analysisId AND a.ragStatus = 'IN_PROGRESS'")
    int markRagDone(@Param("analysisId") UUID analysisId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Analysis a SET a.ragStatus = 'FAILED' " +
            "WHERE a.id = :analysisId AND a.ragStatus = 'IN_PROGRESS'")
    int markRagFail(@Param("analysisId") UUID analysisId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Analysis a SET a.ragStatus = 'NONE' " +
            "WHERE a.id = :analysisId")
    int markRagNone(UUID analysisId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Analysis a SET a.accidentSituation = :accidentSituation, a.accidentExplain = :accidentExplain " +
            "WHERE a.id = :analysisId")
    int setAnalysisSummary(@Param("analysisId") UUID analysisId,
                           @Param("accidentSituation") String accidentSituation,
                           @Param("accidentExplain") String accidentExplain);
}