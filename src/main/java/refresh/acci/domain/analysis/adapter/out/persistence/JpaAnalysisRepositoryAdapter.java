package refresh.acci.domain.analysis.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.common.PageResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaAnalysisRepositoryAdapter implements AnalysisRepositoryPort {

    private final AnalysisRepository analysisRepository;

    @Override
    public Analysis saveAndFlush(Analysis analysis) {
        return analysisRepository.saveAndFlush(analysis);
    }

    @Override
    public Analysis getById(UUID id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("해당 분석을 찾을 수 없습니다. ID: {}", id);
                    return new CustomException(ErrorCode.ANALYSIS_NOT_FOUND);
                });
    }

    @Override
    public PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Analysis> analysisPage = analysisRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<AnalysisSummaryResponse> responsePage = analysisPage.map(AnalysisSummaryResponse::from);
        return PageResponse.of(responsePage);
    }
}
