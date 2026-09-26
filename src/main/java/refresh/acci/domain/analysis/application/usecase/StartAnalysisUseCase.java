package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    @Transactional
    public AnalysisUploadResponse startAnalysis(MultipartFile video, CustomUserDetails userDetails) {
        // 비디오 파일 유효성 검사
        if (video == null || video.isEmpty()) throw new CustomException(ErrorCode.VIDEO_FILE_MISSING);
        // 사용자 ID 추출 (인증된 사용자일 경우)
        Long userId = null;
        if (userDetails != null) userId = userDetails.getId();

        // 새로운 Analysis 엔티티 생성 및 저장
        Analysis analysis = analysisRepository.saveAndFlush(Analysis.of(userId));
        // S3 키 생성
        String ext = FilenameUtils.getExtension(video.getOriginalFilename());
        String s3Key = "analysis/" + analysis.getId() + "/original." + ext;
        // 임시 파일로 저장 후 비동기 분석 작업 실행
        Path tempFilePath = tempFile.saveToTempFile(video, analysis.getId());

        try {
            // S3 업로드
            videoStorage.uploadFile(s3Key, tempFilePath);
            analysis.attachVideoS3Key(s3Key);
        } catch (SdkException e) {
            analysis.failAnalysis();
            tempFile.deleteTempFile(tempFilePath);

            log.error("S3 업로드 실패 상세", e);
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }

        // 비동기 분석 작업은 커밋 이후에 실행
        // 커밋 전에 실행하면 워커가 아직 커밋되지 않은 Analysis 를 조회하지 못해 작업이 실패하거나 PROCESSING 상태로 고착됨
        runAfterCommit(() -> dispatchAnalysis(analysis.getId(), tempFilePath));

        return AnalysisUploadResponse.of(analysis);
    }

    private void dispatchAnalysis(UUID analysisId, Path tempFilePath) {
        try {
            executor.execute(() -> runAnalysisUseCase.runAnalysis(analysisId, tempFilePath));
        } catch (RejectedExecutionException e) {
            // 거절 처리: 상태 FAILED(이미 커밋된 뒤이므로 새 트랜잭션), SSE 전송, 파일 삭제
            Analysis failed = queryAnalysisUseCase.failInNewTransaction(analysisId);
            analysisEvent.sendStatus(failed);
            tempFile.deleteTempFile(tempFilePath);

            log.error("분석 작업이 너무 많아 요청이 거절되었습니다. 분석 ID: {}", analysisId);
            throw new CustomException(ErrorCode.TOO_MANY_ANALYSIS_REQUESTS);
        }
    }

    // afterCommit 콜백에서 던진 예외는 호출자에게 그대로 전파되므로 거절 시 429 응답이 유지됨
    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
