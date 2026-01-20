package refresh.acci.domain.precedent.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.domain.precedent.model.enums.PrecedentType;
import refresh.acci.global.common.BaseTime;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "precedent")
public class Precedent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String caseNumber;

    @Column(nullable = false)
    private String court;

    @Column(nullable = false)
    private LocalDate dateOfJudgment;

    @Column(nullable = false)
    private Long accidentRateA;

    @Column(nullable = false)
    private Long accidentRateB;

    @Column(name = "accident_title",nullable = false)
    private String title;

    @Column(name = "accident_overview", nullable = false)
    private String overview;

    @Column(name = "accident_description", nullable = false)
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private PrecedentType precedentType;
}
