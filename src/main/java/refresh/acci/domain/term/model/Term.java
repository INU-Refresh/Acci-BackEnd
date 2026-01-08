package refresh.acci.domain.term.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.global.common.SoftDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "term")
public class Term extends SoftDelete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(name = "term_content")
    private String content;

    @Column(name = "term_agreed", nullable = false)
    private Boolean agreed = false;

    @Column(name = "term_version", nullable = false)
    private String version;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;
}
