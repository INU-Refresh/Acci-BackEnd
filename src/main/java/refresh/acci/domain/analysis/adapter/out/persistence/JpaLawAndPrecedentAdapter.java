package refresh.acci.domain.analysis.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.application.port.out.LawAndPrecedentPort;
import refresh.acci.domain.law.infra.LawRepository;
import refresh.acci.domain.law.model.Law;
import refresh.acci.domain.precedent.infra.PrecedentRepository;
import refresh.acci.domain.precedent.model.Precedent;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaLawAndPrecedentAdapter implements LawAndPrecedentPort {

    private final LawRepository lawRepository;
    private final PrecedentRepository precedentRepository;

    @Override
    @Transactional
    public boolean saveRelatedLawsAndPrecedents(UUID analysisId, RagSummaryResponse ragSummaryResponse) {
        if (ragSummaryResponse == null) {
            return false;
        }

        lawRepository.deleteByAnalysisId(analysisId);
        List<Law> laws = ragSummaryResponse.relatedLaws().stream()
                .map(r -> Law.of(analysisId, r.lawName(), r.lawContent()))
                .toList();
        lawRepository.saveAll(laws);

        precedentRepository.deleteByAnalysisId(analysisId);
        List<Precedent> precedents = ragSummaryResponse.precedentCases().stream()
                .map(p -> Precedent.of(analysisId, p.caseName(), p.summary(), p.dateOfJudgment()))
                .toList();
        precedentRepository.saveAll(precedents);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RagSummaryResponse.RelatedLawsResponse> getRelatedLawsByAnalysisId(UUID analysisId) {
        return lawRepository.findAllLawsByAnalysisId(analysisId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RagSummaryResponse.PrecedentCasesResponse> getPrecedentCasesByAnalysisId(UUID analysisId) {
        return precedentRepository.findAllPrecedentsByAnalysisId(analysisId);
    }
}
