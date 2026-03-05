package refresh.acci.domain.vectorDb.presentation.dto.res;

import refresh.acci.domain.analysis.model.Analysis;

import java.time.LocalDate;
import java.util.List;

public record RagSummaryResponse(

        String accidentSituation,

        String accidentExplain,

        List<RelatedLawsResponse> relatedLaws,

        List<PrecedentCasesResponse> precedentCases

) {
    public record PrecedentCasesResponse(
            String caseName,
            String summary,
            LocalDate dateOfJudgment
    ) {}

    public record RelatedLawsResponse(
            String lawName,
            String lawContent
    ) {}

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
