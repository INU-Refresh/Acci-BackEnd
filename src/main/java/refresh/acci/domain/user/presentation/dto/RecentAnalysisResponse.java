package refresh.acci.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecentAnalysisResponse {
    private final UUID analysisId;
    private final AnalysisStatus analysisStatus;
    private final boolean isCompleted;
    private final Long accidentRateA;
    private final Long accidentRateB;
    private final LocalDateTime createdAt;

    public static RecentAnalysisResponse from(Analysis analysis) {
        return RecentAnalysisResponse.builder()
                .analysisId(analysis.getId())
                .analysisStatus(analysis.getAnalysisStatus())
                .isCompleted(analysis.isCompleted())
                .accidentRateA(analysis.getAccidentRateA())
                .accidentRateB(analysis.getAccidentRateB())
                .createdAt(analysis.getCreatedAt())
                .build();
    }
}
