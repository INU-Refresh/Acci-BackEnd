package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.infra.persistence.AnalysisRepository;
import refresh.acci.domain.analysis.model.Analysis;

@Service
@RequiredArgsConstructor
public class AnalysisCommandService {

    private final AnalysisRepository analysisRepository;

    @Transactional
    public Analysis saveAndFlushNewAnalysis(Analysis analysis) {
        return analysisRepository.saveAndFlush(analysis);
    }
}
