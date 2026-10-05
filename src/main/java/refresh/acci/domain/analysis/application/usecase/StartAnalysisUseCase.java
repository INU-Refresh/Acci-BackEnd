package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.analysis.application.port.out.*;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import software.amazon.awssdk.core.exception.SdkException;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartAnalysisUseCase {

    private final AnalysisRepositoryPort analysisRepository;
    private final TempFilePort tempFile;
    private final VideoStoragePort videoStorage;
    private final TaskExecutorPort executor;
    private final AnalysisEventPort analysisEvent;
    private final ProcessAnalysisAIJobUseCase runAnalysisUseCase;
    private final QueryAnalysisUseCase queryAnalysisUseCase;

    // 메서드 전체를 하나의 트랜잭션으로 묶지 않는다.
    // - Analysis 저장을 먼저 커밋해야 비동기 워커가 해당 행을 조회할 수 있고 (커밋 전 조회 → PROCESSING 고착 방지)
    // - S3 업로드(외부 호출) 동안 DB 커넥션을 점유하지 않는다.
    // 각 DB 작업은 Repository / QueryAnalysisUseCase 의 짧은 트랜잭션으로 처리한다.
    public AnalysisUploadResponse startAnalysis(MultipartFile video, CustomUserDetails userDetails) {
        // 비디오 파일 유효성 검사
        if (video == null || video.isEmpty()) throw new CustomException(ErrorCode.VIDEO_FILE_MISSING);
        // 사용자 ID 추출 (인증된 사용자일 경우)
        Long userId = null;
        if (userDetails != null) userId = userDetails.getId();

        // 새로운 Analysis 엔티티 생성 및 저장 (즉시 커밋)
        UUID analysisId = analysisRepository.saveAndFlush(Analysis.of(userId)).getId();
        // S3 키 생성
        String ext = FilenameUtils.getExtension(video.getOriginalFilename());
        String s3Key = "analysis/" + analysisId + "/original." + ext;

        Path tempFilePath = null;
        Analysis analysis;
        try {
            // 임시 파일로 저장 후 S3 업로드 (트랜잭션 밖)
            tempFilePath = tempFile.saveToTempFile(video, analysisId);
            videoStorage.uploadFile(s3Key, tempFilePath);
            analysis = queryAnalysisUseCase.attachVideoS3Key(analysisId, s3Key);
        } catch (SdkException e) {
            failAndCleanUp(analysisId, tempFilePath);

            log.error("S3 업로드 실패 상세", e);
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        } catch (RuntimeException e) {
            // 이미 커밋된 Analysis 가 PROCESSING 으로 남지 않도록 실패 처리 후 원본 예외 전파
            failAndCleanUp(analysisId, tempFilePath);
            throw e;
        }

        // 비동기 분석 작업 실행 (Analysis 는 이미 커밋된 상태)
        final Path videoPath = tempFilePath;
        try {
            executor.execute(() -> runAnalysisUseCase.runAnalysis(analysisId, videoPath));
        } catch (RejectedExecutionException e) {
            // 거절 처리: 상태 FAILED, SSE 전송, 파일 삭제
            analysisEvent.sendStatus(failAndCleanUp(analysisId, tempFilePath));

            log.error("분석 작업이 너무 많아 요청이 거절되었습니다. 분석 ID: {}", analysisId);
            throw new CustomException(ErrorCode.TOO_MANY_ANALYSIS_REQUESTS);
        }

        return AnalysisUploadResponse.of(analysis);
    }

    private Analysis failAndCleanUp(UUID analysisId, Path tempFilePath) {
        Analysis failed = queryAnalysisUseCase.fail(analysisId);
        if (tempFilePath != null) tempFile.deleteTempFile(tempFilePath);
        return failed;
    }
}
