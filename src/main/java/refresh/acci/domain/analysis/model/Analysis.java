package refresh.acci.domain.analysis.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import refresh.acci.domain.analysis.model.enums.AccidentType;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.global.common.BaseTime;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "analysis")
public class Analysis extends BaseTime {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "ai_job_id", unique = true)
    private String aiJobId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "video_s3_key")
    private String videoS3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "accident_type")
    private AccidentType accidentType;

    @Column(name = "accident_rate_A")
    private Long accidentRateA;

    @Column(name = "accident_rate_B")
    private Long accidentRateB;

    @Column(name = "place")
    private String place;

    @Column(name = "situation")
    private String situation;

    @Column(name = "vehicle_A_situation")
    private String vehicleASituation;

    @Column(name = "vehicle_B_situation")
    private String vehicleBSituation;

    @Enumerated(EnumType.STRING)
    @Column(name = "accident_status")
    private AnalysisStatus analysisStatus;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    private Analysis(Long userId, AnalysisStatus analysisStatus) {
        this.userId = userId;
        this.analysisStatus = analysisStatus;
    }

    public static Analysis of(Long userId) {
        return new Analysis(userId, AnalysisStatus.PROCESSING);
    }

    public void attachVideoS3Key(String videoS3Key) {
        this.videoS3Key = videoS3Key;
    }

    public void failAnalysis() {
        this.analysisStatus = AnalysisStatus.FAILED;
        this.isCompleted = true;
    }

    public void markProcessing(String aiJobId) {
        this.aiJobId = aiJobId;
        this.analysisStatus = AnalysisStatus.PROCESSING;
        this.isCompleted = false;
    }

    public void completeAnalysisFromAi(AiResultResponse result) {
        this.accidentType = AccidentType.fromInt(result.accident_type());
        this.accidentRateA = (long) result.vehicle_A_fault();
        this.accidentRateB = (long) result.vehicle_B_fault();
        this.place = result.place();
        this.situation = result.situation();
        this.vehicleASituation = result.vehicle_a();
        this.vehicleBSituation = result.vehicle_b();
        this.analysisStatus = AnalysisStatus.COMPLETED;
        this.isCompleted = true;
    }
}
