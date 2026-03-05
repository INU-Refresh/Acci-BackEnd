package refresh.acci.domain.law.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.global.common.BaseTime;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law")
public class Law extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;

    @Column(name = "law_name", nullable = false)
    private String name;

    @Column(name = "law_content", nullable = false)
    private String content;

    private Law (UUID analysisId, String name, String content) {
        this.analysisId = analysisId;
        this.name = name;
        this.content = content;
    }

    public static Law of(UUID analysisId, String name, String content) {
        return new Law(analysisId, name, content);
    }
}
