package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.domain.analysis.infra.file.TempVideoStore;
import refresh.acci.domain.analysis.infra.support.LoadingTipsProvider;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisWorkerService analysisWorkerService;
    private final TempVideoStore tempVideoStore;
    private final LoadingTipsProvider loadingTipsProvider;
    private final AnalysisQueryService analysisQueryService;
    private final AnalysisCommandService analysisCommandService;
    private final AnalysisSseService analysisSseService;
    private final Executor analysisExecutor;

    @Transactional
    public AnalysisUploadResponse anaylze(MultipartFile video, CustomUserDetails userDetails) {
        if (video == null || video.isEmpty()) throw new CustomException(ErrorCode.VIDEO_FILE_MISSING);

        Long userId = null;
        if (userDetails != null) userId = userDetails.getId();

        Analysis analysis = analysisCommandService.saveAndFlushNewAnalysis(Analysis.of(userId));

        // 임시 파일로 저장 후 비동기 분석 작업 실행
        Path tempFilePath = tempVideoStore.saveToTempFile(video, analysis.getId());

        // 비동기 분석 작업 실행
        try {
            analysisExecutor.execute(() -> analysisWorkerService.runAnalysis(analysis.getId(), tempFilePath));
        } catch (RejectedExecutionException e) {
            // 거절 처리: 상태 FAILED, SSE 전송, 파일 삭제
            Analysis failedAnalysis = analysisCommandService.fail(analysis.getId());
            analysisSseService.send(failedAnalysis.getId(), "status",
                    Map.of("analysisId", failedAnalysis.getId(),
                           "status", failedAnalysis.getAnalysisStatus(),
                           "isCompleted", failedAnalysis.isCompleted()));
            tempVideoStore.deleteFile(tempFilePath);

            log.error("분석 작업이 너무 많아 요청이 거절되었습니다. 분석 ID: {}", failedAnalysis.getId());
            throw new CustomException(ErrorCode.TOO_MANY_ANALYSIS_REQUESTS);
        }

        return AnalysisUploadResponse.of(analysis);
    }

    @Transactional(readOnly = true)
    public String getLoadingTips() {
        return loadingTipsProvider.getRandomTip();
    }

    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResult(UUID analysisId) {
        return analysisQueryService.getAnalysisResult(analysisId);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultResponse> getUserAnalysisHistory(CustomUserDetails userDetails) {
        return analysisQueryService.getUserAnalysisHistory(userDetails.getId());
    }
}
