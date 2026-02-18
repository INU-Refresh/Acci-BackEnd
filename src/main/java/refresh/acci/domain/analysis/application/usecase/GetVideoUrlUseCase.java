package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.application.port.out.VideoStoragePort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetVideoUrlUseCase {

    private final AnalysisRepositoryPort analysisRepository;
    private final VideoStoragePort videoStorage;

    private static final Duration DEFAULT_EXPIRES = Duration.ofMinutes(10);

    public String getVideoUrl(UUID analysisId, CustomUserDetails userDetails) {
        Analysis analysis = analysisRepository.getById(analysisId);

        if (userDetails == null || analysis.getUserId() == null || !analysis.getUserId().equals(userDetails.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);
        }

        if (analysis.getVideoS3Key() == null || analysis.getVideoS3Key().isBlank()) {
            throw new CustomException(ErrorCode.VIDEO_NOT_FOUND);
        }

        return videoStorage.generatePresignedUrl(analysis.getVideoS3Key(), DEFAULT_EXPIRES);
    }
}
