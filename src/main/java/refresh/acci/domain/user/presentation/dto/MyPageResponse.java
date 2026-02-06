package refresh.acci.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.user.model.User;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {
    private final String name;
    private final String email;
    private final String profileImage;
    private final String role;
    private final List<RecentAnalysisResponse> recentAnalyses;
    private final List<RecentRepairEstimateResponse> recentRepairEstimates;

    public static MyPageResponse of(User user, List<RecentAnalysisResponse> recentAnalyses, List<RecentRepairEstimateResponse> recentRepairEstimates) {
        return MyPageResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .recentAnalyses(recentAnalyses)
                .recentRepairEstimates(recentRepairEstimates)
                .build();
    }
}
