package refresh.acci.domain.user.application.facade;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.domain.analysis.application.AnalysisQueryService;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.auth.application.AuthService;
import refresh.acci.domain.repair.application.service.RepairEstimateQueryService;
import refresh.acci.domain.repair.infra.persistence.DamageDetailRepository;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.user.application.service.UserCommandService;
import refresh.acci.domain.user.application.service.UserQueryService;
import refresh.acci.domain.user.model.User;
import refresh.acci.domain.user.presentation.dto.MyPageResponse;
import refresh.acci.domain.user.presentation.dto.RecentAnalysisResponse;
import refresh.acci.domain.user.presentation.dto.RecentRepairEstimateResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final AuthService authService;
    private final AnalysisQueryService analysisQueryService;
    private final RepairEstimateQueryService repairEstimateQueryService;
    private final DamageDetailRepository damageDetailRepository;

    /**
     * 사용자 정보 조회
     */
    public MyPageResponse getMyPage(Long userId) {
        User user = userQueryService.getUserById(userId);

        //최근 분석 3개 조회
        List<Analysis> recentAnalyses = analysisQueryService.getRecentAnalyses(userId);
        List<RecentAnalysisResponse> recentAnalysisResponses = recentAnalyses.stream()
                .map(RecentAnalysisResponse::from)
                .toList();

        //최근 수리비 견적 3개 조회
        List<RepairEstimate> recentEstimates = repairEstimateQueryService.getRecentEstimates(userId);
        List<RecentRepairEstimateResponse> recentEstimateResponses = buildRecentEstimateResponses(recentEstimates);

        return MyPageResponse.of(user, recentAnalysisResponses, recentEstimateResponses);
    }

    /**
     * 회원 탈퇴
     * 1. User 계정 삭제 (soft delete)
     * 2. 인증 정보 제거 (쿠키 삭제)
     */
    public void deleteUserAccount(Long userId, HttpServletResponse response) {
        userCommandService.deleteAccount(userId);
        authService.clearAuthentication(response);
    }


    //최근 수리비 견적 Response 리스트 생성 (3개 미만일 경우 null 패딩)
    private List<RecentRepairEstimateResponse> buildRecentEstimateResponses(List<RepairEstimate> estimates) {
        if (estimates.isEmpty()) {
            return Collections.emptyList();
        }

        // estimateId 리스트 추출
        List<UUID> estimateIds = estimates.stream()
                .map(RepairEstimate::getId)
                .toList();

        // 한 번에 모든 DamageDetail 조회 후 Map으로 그룹핑 (N+1 방지)
        Map<UUID, List<DamageDetail>> damageDetailMap = damageDetailRepository
                .findByRepairEstimateIdIn(estimateIds)
                .stream()
                .collect(Collectors.groupingBy(DamageDetail::getRepairEstimateId));

        // RepairEstimate를 Response로 변환
        return estimates.stream()
                .map(estimate -> {
                    List<DamageDetail> damageDetails = damageDetailMap.getOrDefault(estimate.getId(), Collections.emptyList());
                    return RecentRepairEstimateResponse.of(estimate, damageDetails);
                })
                .toList();
    }
}
