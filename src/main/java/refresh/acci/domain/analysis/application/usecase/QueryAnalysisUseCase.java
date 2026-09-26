package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.common.PageResponse;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryAnalysisUseCase {

    private final AnalysisRepositoryPort analysisRepository;

    public PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size) {
        return analysisRepository.getUserAnalyses(userId, page, size);
    }

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

    // 트랜잭션 커밋 이후(afterCommit) 콜백에서 호출 — 기존 트랜잭션은 이미 커밋되어 변경을 반영할 수 없으므로 새 트랜잭션으로 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Analysis failInNewTransaction(UUID analysisId) {
        return fail(analysisId);
    }
}
