package refresh.acci.domain.analysis.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import refresh.acci.domain.analysis.model.enums.AccidentStatus;
import refresh.acci.domain.analysis.model.enums.AccidentType;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "analysis")
public class Analysis {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "accident_rate")
    private Long accidentRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "accident_type")
    private AccidentType accidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "accident_status")
    private AccidentStatus accidentStatus;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    private Analysis(Long userId, AccidentStatus accidentStatus) {
        this.userId = userId;
        this.accidentStatus = accidentStatus;
    }

    public static Analysis of(Long userId) {
        return new Analysis(userId, AccidentStatus.PROCESSING);
    }

    public void completeAnalysis(Long accidentRate, AccidentType accidentType) {
        this.accidentRate = accidentRate;
        this.accidentType = accidentType;
        this.accidentStatus = AccidentStatus.COMPLETED;
        this.isCompleted = true;
    }

    public void failAnalysis() {
        this.accidentStatus = AccidentStatus.FAILED;
        this.isCompleted = true;
    }
}
