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

import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisWorkerService analysisWorkerService;
    private final TempVideoStore tempVideoStore;
    private final LoadingTipsProvider loadingTipsProvider;
    private final AnalysisQueryService analysisQueryService;
    private final AnalysisCommandService analysisCommandService;

    @Transactional
    public AnalysisUploadResponse anaylze(MultipartFile video, CustomUserDetails userDetails) {
        Analysis analysis = analysisCommandService.saveAndFlushNewAnalysis(Analysis.of(userDetails.getId()));

        // 임시 파일로 저장 후 비동기 분석 작업 실행
        Path tempFilePath = tempVideoStore.saveToTempFile(video, analysis.getId());
        analysisWorkerService.runAnalysis(analysis.getId(), tempFilePath);

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
}
