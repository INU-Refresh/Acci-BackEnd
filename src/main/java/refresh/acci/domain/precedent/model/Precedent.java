package refresh.acci.domain.precedent.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.global.common.BaseTime;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "precedent")
public class Precedent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;

    @Column(name = "case_name")
    private String caseName;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "date_of_judgment")
    private LocalDate dateOfJudgment;

    private Precedent(UUID analysisId, String caseName, String summary, LocalDate dateOfJudgment) {
        this.analysisId = analysisId;
        this.caseName = caseName;
        this.summary = summary;
        this.dateOfJudgment = dateOfJudgment;
    }

    public static Precedent of(UUID analysisId, String caseName, String summary, LocalDate dateOfJudgment) {
        return new Precedent(analysisId, caseName, summary, dateOfJudgment);
    }
}
