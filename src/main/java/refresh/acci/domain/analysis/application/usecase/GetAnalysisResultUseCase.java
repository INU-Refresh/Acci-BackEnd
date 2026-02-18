package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAnalysisResultUseCase {

    private final AnalysisRepositoryPort analysisRepository;

    public AnalysisResultResponse getAnalysisResult(UUID analysisId, CustomUserDetails userDetails) {
        Analysis analysis = analysisRepository.getById(analysisId);

        if (userDetails == null || analysis.getUserId() == null || !analysis.getUserId().equals(userDetails.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);
        }

        return AnalysisResultResponse.of(analysis);
    }
}
