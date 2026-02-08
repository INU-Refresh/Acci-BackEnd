package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.domain.analysis.infra.file.TempVideoStore;
import refresh.acci.domain.analysis.infra.support.LoadingTipsProvider;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.file.application.S3FileService;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.common.PageResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import software.amazon.awssdk.core.exception.SdkException;

import java.nio.file.Path;
import java.time.Duration;
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
    private final S3FileService s3FileService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public AnalysisUploadResponse anaylze(MultipartFile video, CustomUserDetails userDetails) {
        if (video == null || video.isEmpty()) throw new CustomException(ErrorCode.VIDEO_FILE_MISSING);

        Long userId = null;
        if (userDetails != null) userId = userDetails.getId();

        // 새로운 Analysis 엔티티 생성 및 저장
        Analysis analysis = analysisCommandService.saveAndFlushNewAnalysis(Analysis.of(userId));

        // S3 키 생성
        String ext = FilenameUtils.getExtension(video.getOriginalFilename());
        String s3Key = "analysis/" + analysis.getId() + "/original." + ext;

        // 임시 파일로 저장 후 비동기 분석 작업 실행
        Path tempFilePath = tempVideoStore.saveToTempFile(video, analysis.getId());

        // 비동기 분석 작업 실행
        try {
            // S3 업로드
            s3FileService.uploadFile(bucket, s3Key, tempFilePath);
            analysis.attachVideoS3Key(s3Key);

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
        } catch (SdkException e) {
            analysisCommandService.fail(analysis.getId());
            tempVideoStore.deleteFile(tempFilePath);
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }

        return AnalysisUploadResponse.of(analysis);
    }

    @Transactional(readOnly = true)
    public String getLoadingTips() {
        return loadingTipsProvider.getRandomTip();
    }

    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResult(UUID analysisId) {
        Analysis analysis = analysisQueryService.getAnalysis(analysisId);
        return AnalysisResultResponse.of(analysis);
    }

    @Transactional(readOnly = true)
    public String getVideoUrl(UUID analysisId, CustomUserDetails userDetails) {
        Analysis analysis = analysisQueryService.getAnalysis(analysisId);

        // 권한 체크 (본인 영상만)
        if (!analysis.getUserId().equals(userDetails.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);
        }

        if (analysis.getVideoS3Key() == null) {
            throw new CustomException(ErrorCode.VIDEO_NOT_FOUND);
        }

        return s3FileService.generatePresignedUrl(
                bucket,
                analysis.getVideoS3Key(),
                Duration.ofMinutes(10)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Analysis> analysisPage = analysisQueryService.getUserAnalyses(userId, pageable);
        Page<AnalysisSummaryResponse> responsePage = analysisPage.map(AnalysisSummaryResponse::from);
        return PageResponse.of(responsePage);
    }
}
