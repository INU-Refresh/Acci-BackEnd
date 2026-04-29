package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AccidentAiResultResponse;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.application.port.out.LawAndPrecedentPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.enums.RagStatus;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.domain.vectorDb.application.RagSearchService;
import refresh.acci.domain.vectorDb.application.RagSummaryService;
import refresh.acci.domain.vectorDb.presentation.dto.res.PrecedentCasesResponse;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagInfoResponse;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;
import refresh.acci.domain.vectorDb.presentation.dto.res.RelatedLawsResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultUseCase {

    private final AnalysisRepositoryPort analysisRepository;
    private final LawAndPrecedentPort lawAndPrecedentPort;
    private final RagSearchService ragSearchService;
    private final RagSummaryService ragSummaryService;

    @Transactional
    public AnalysisResultResponse getAnalysisResult(UUID analysisId, CustomUserDetails userDetails) {
        Analysis analysis = analysisRepository.getById(analysisId);
        if (analysis.getAnalysisStatus() == AnalysisStatus.PROCESSING) {
            throw new CustomException(ErrorCode.ANALYSIS_NOT_COMPLETED);
        }

        // 분석 결과에 대한 접근 권한 검증
        if (userDetails == null || analysis.getUserId() == null || !analysis.getUserId().equals(userDetails.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);
        }

        // AI 분석 결과 응답
        AccidentAiResultResponse aiResult = AccidentAiResultResponse.of(analysis);

        // DONE이면 결과 반환
        if (analysis.getRagStatus() == RagStatus.DONE) {
            return buildResultFromSaved(analysis, aiResult);
        }

        // 락 잡기 시도 (CAS)
        boolean lockAcquired = analysisRepository.tryMarkRagInProgress(analysisId);

        if (!lockAcquired) {
            // 누가 이미 진행중이거나, 방금 완료했을 수 있음 → 다시 한번 최신 조회
            Analysis latestAnalysis = analysisRepository.getById(analysisId);
            if (latestAnalysis.getRagStatus() == RagStatus.DONE) {
                return buildResultFromSaved(latestAnalysis, aiResult);
            }
            throw new CustomException(ErrorCode.RAG_ALREADY_PROGRESS);
        }

        // 락 획득 후 새로 조회
        Analysis latestAnalysis = analysisRepository.getById(analysisId);
        // 새로 RAG 수행
        RagSummaryResponse summary = summarizeRagInfo(latestAnalysis);

        // RAG 요약이 null인 경우 → RAG 실패로 간주하고 반환
        if (summary == null) {
            return AnalysisResultResponse.of(latestAnalysis, aiResult, null);
        }

        if (lawAndPrecedentPort.saveRelatedLawsAndPrecedents(analysisId, summary)) {
            analysisRepository.markRagDone(analysisId);
            analysisRepository.setAnalysisSummary(analysisId, summary.accidentSituation(), summary.accidentExplain());
        } else {
            analysisRepository.markRagFailed(analysisId);
        }
        Analysis refreshed = analysisRepository.getById(analysisId);
        return AnalysisResultResponse.of(refreshed, aiResult, summary);
    }

    private RagSummaryResponse summarizeRagInfo(Analysis analysis) {
        try {
            RagInfoResponse ragInfoResponse = ragSearchService.search(analysis);
            return ragSummaryService.summarize(ragInfoResponse);
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.GEMINI_RATE_LIMITED) {
                analysisRepository.markRagNone(analysis.getId());
                throw e;
            }
            analysisRepository.markRagFailed(analysis.getId());
            log.warn("RAG summarization failed: code={}", e.getMessage(), e);
            return null;
        }
    }

    private AnalysisResultResponse buildResultFromSaved(Analysis analysis, AccidentAiResultResponse aiResult) {
        List<RelatedLawsResponse> relatedLawsResponses = lawAndPrecedentPort.getRelatedLawsByAnalysisId(analysis.getId());
        List<PrecedentCasesResponse> precedentCasesResponses = lawAndPrecedentPort.getPrecedentCasesByAnalysisId(analysis.getId());
        RagSummaryResponse ragSummaryResponse = RagSummaryResponse.of(analysis, relatedLawsResponses, precedentCasesResponses);
        return AnalysisResultResponse.of(analysis, aiResult, ragSummaryResponse);
    }
}
