package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.infra.persistence.AnalysisRepository;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisQueryService {

    private final AnalysisRepository analysisRepository;

    public Analysis getAnalysis(UUID analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> {
                    log.warn("해당 분석을 찾을 수 없습니다. ID: {}", analysisId);
                    return new CustomException(ErrorCode.ANALYSIS_NOT_FOUND);
                });
    }

    public List<Analysis> getUserAnalysisHistory(Long userId) {
        return analysisRepository.findAllByUserId(userId);
    }

    public List<Analysis> getRecentAnalyses(Long userId) {
        return analysisRepository.findTop3ByUserIdOrderByCreatedAtDesc(userId);
    }
}
