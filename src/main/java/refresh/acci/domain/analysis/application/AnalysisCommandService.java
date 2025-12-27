package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.infra.persistence.AnalysisRepository;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCommandService {

    private final AnalysisRepository analysisRepository;

    @Transactional
    public Analysis saveAndFlushNewAnalysis(Analysis analysis) {
        return analysisRepository.saveAndFlush(analysis);
    }

    @Transactional
    public Analysis completeAndGetAnalysis(UUID analysisId, AnalysisResultResponse response) {
        Analysis analysis = getAnalysis(analysisId);
        analysis.completeAnalysis(response.accidentRate(), response.accidentType());
        return analysis;
    }

    @Transactional
    public Analysis fail(UUID analysisId) {
        Analysis analysis = getAnalysis(analysisId);
        analysis.failAnalysis();
        return analysis;
    }

    private Analysis getAnalysis(UUID analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> {
                    log.warn("해당 분석을 찾을 수 없습니다. ID: {}", analysisId);
                    return new CustomException(ErrorCode.ANALYSIS_NOT_FOUND);
                });
    }
}
