package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.global.common.PageResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserAnalysesUseCase {

    private final AnalysisRepositoryPort analysisRepository;

    public PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size) {
        return analysisRepository.getUserAnalyses(userId, page, size);
    }
}
