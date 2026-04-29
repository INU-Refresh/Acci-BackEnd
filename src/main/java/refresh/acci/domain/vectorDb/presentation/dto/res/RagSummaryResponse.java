package refresh.acci.domain.vectorDb.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.List;

public record RagSummaryResponse(

        @Schema(description = "사고 상황", example = "도로를 후행하여 진행하는 뒤차가 동일 방향에서 선행하는 앞차를 추돌한 사고이며, " +
                "앞차가 정지한 직후에 발생한 경우를 포함한다.")
        String accidentSituation,

        @Schema(description = "사고 설명", example = "추돌 사고는 기본적으로 추돌 차량의 전방주시 태만과 안전거리 미확보로 인하여 발생한 것으로 판단한다. " +
                "다만 앞차가 제동등 고장, 이유 없는 급정지, 또는 편도 3차로 이상의 주행차로에서 이유 없이 정지한 경우에는 앞차의 과실을 가산할 수 있다. " +
                "주택가나 상점가처럼 보행자가 많아 급제동이 예상되는 장소에서는 뒤차의 주의 의무가 더 크게 고려된다. " +
                "또한 앞차가 위험방지 등 부득이한 사유 없이 급제동하여 사고가 발생한 경우에도 앞차의 과실을 적용할 수 있다.")
        String accidentExplain,

        @Schema(description = "관련 법률 정보")
        List<RelatedLawsResponse> relatedLaws,

        @Schema(description = "유사 판례 정보")
        List<PrecedentCasesResponse> precedentCases

) {

    public static RagSummaryResponse of(
            String accidentSituation,
            String accidentExplain,
            List<RelatedLawsResponse> relatedLaws,
            List<PrecedentCasesResponse> precedentCases) {
        return new RagSummaryResponse(
                accidentSituation,
                accidentExplain,
                relatedLaws,
                precedentCases);
    }

    public static RagSummaryResponse of(Analysis analysis,
                                        List<RelatedLawsResponse> relatedLawsResponse,
                                        List<PrecedentCasesResponse> precedentCasesResponses) {
        return new RagSummaryResponse(
                analysis.getAccidentSituation(),
                analysis.getAccidentExplain(),
                relatedLawsResponse,
                precedentCasesResponses);
    }
}
