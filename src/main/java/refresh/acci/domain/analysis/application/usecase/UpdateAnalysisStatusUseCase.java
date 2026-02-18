package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateAnalysisStatusUseCase {

    private final AnalysisRepositoryPort analysisRepository;

    @Transactional
    public Analysis markProcessing(UUID analysisId, String aiJobId) {
        Analysis analysis = analysisRepository.getById(analysisId);
        analysis.markProcessing(aiJobId);
        return analysis;
    }

    @Transactional
    public Analysis completeFromAi(UUID analysisId, AiResultResponse result) {
        Analysis analysis = analysisRepository.getById(analysisId);
        analysis.completeAnalysisFromAi(result);
        return analysis;
    }

    @Transactional
    public Analysis fail(UUID analysisId) {
        Analysis analysis = analysisRepository.getById(analysisId);
        analysis.failAnalysis();
        return analysis;
    }
}
