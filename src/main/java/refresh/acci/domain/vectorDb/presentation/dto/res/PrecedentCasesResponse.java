package refresh.acci.domain.vectorDb.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record PrecedentCasesResponse(

        @Schema(description = "판례 이름", example = "대법원 97다41639 판결")
        String caseName,

        @Schema(description = "판례 요약", example = "안전거리 확보 의무는 앞차가 제동기의 제동력에 의하여 정지한 경우뿐만 아니라 제동기 이외의 작용에 의하여 갑자기 정지한 경우에도 적용된다.")
        String summary,

        @Schema(description = "판례 날짜", example = "1997-11-25")
        LocalDate dateOfJudgment
) {
}
