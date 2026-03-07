package refresh.acci.domain.vectorDb.presentation.dto.res;

import java.time.LocalDate;

public record PrecedentCasesResponse(
        String caseName,
        String summary,
        LocalDate dateOfJudgment
) {
}
