package refresh.acci.domain.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.domain.user.presentation.dto.MyPageResponse;
import refresh.acci.global.exception.ErrorResponseEntity;

@Tag(name = "User (사용자)", description = "User (사용자) 관련 API")
public interface UserApiSpecification {

    @Operation(
            summary = "내 정보(마이페이지) 조회",
            description = "현재 로그인한 사용자의 프로필 정보와 최근 활동 내역을 조회합니다. <br><br>" +
                    "**조회 데이터:** <br>" +
                    "- **사용자 프로필**: 이름, 이메일, 프로필 이미지, 권한 <br>" +
                    "- **최근 영상 분석**: 최신순 최대 3개 (데이터가 없으면 빈 리스트 반환) <br>" +
                    "- **최근 수리비 견적**: 최신순 최대 3개 (데이터가 없으면 빈 리스트 반환) <br><br>" +
                    "인증(Access Token 쿠키)이 필요하며, 요청 시 `credentials: 'include'` 설정이 필수입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyPageResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "name": "홍길동",
                                                "email": "hong@example.com",
                                                "profileImage": "https://example.com/profile.jpg",
                                                "role": "USER",
                                                "recentAnalyses": [
                                                    {
                                                        "analysisId": "550e8400-e29b-41d4-a716-446655440000",
                                                        "analysisStatus": "COMPLETED",
                                                        "isCompleted": true,
                                                        "accidentRateA": 70,
                                                        "accidentRateB": 30,
                                                        "createdAt": "2024-03-20T10:00:00"
                                                    }
                                                ],
                                                "recentRepairEstimates": []
                                            }
                                            """))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자 정보 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class)))
            })
    ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자를 논리 삭제(Soft Delete) 처리합니다. <br><br>" +
                    "**탈퇴 처리 프로세스:** <br>" +
                    "1. 사용자 테이블의 `deleted` 상태 변경 및 탈퇴 일시 기록 <br>" +
                    "2. 브라우저에 저장된 인증 쿠키(Access/Refresh Token) 즉시 삭제 <br>" +
                    "3. 개인정보 보호법에 따라 6개월간 데이터 보관 후 스케줄러에 의해 물리 삭제 <br><br>" +
                    "인증(Access Token 쿠키)이 필요합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "탈퇴 처리 완료 (응답 바디 없음)"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자 정보 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class)))
            })
    ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response);
}