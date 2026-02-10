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
import refresh.acci.domain.user.presentation.dto.UserInfoResponse;
import refresh.acci.global.exception.ErrorResponseEntity;

@Tag(name = "User (사용자)", description = "User (사용자) 관련 API")
public interface UserApiSpecification {

    @Operation(
            summary = "내 정보 조회 (GNB/프로필용)",
            description = "현재 로그인한 사용자의 **기본 프로필 정보(이름, 이메일, 사진)**를 조회합니다. <br><br>" +
                    "페이지 새로고침이나 이동 시 **상단 바(GNB)**의 프로필 사진 및 이름을 표시하기 위해 사용됩니다. <br>" +
                    "**[참고]** 분석 기록이나 견적 내역은 포함되지 않으며, 해당 정보는 각 도메인 API를 병렬로 호출하여 조회해야 합니다. <br><br>" +
                    "인증(Access Token 쿠키)이 필요하며, 요청 시 `credentials: 'include'` 설정이 필수입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserInfoResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "name": "홍길동",
                                                "email": "hong@example.com",
                                                "profileImage": "https://example.com/profile.jpg",
                                                "role": "USER"
                                            }
                                            """))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 없음/만료)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 401,
                                                "name": "JWT_ENTRY_POINT",
                                                "message": "인증되지 않은 사용자입니다.",
                                                "errors": null
                                            }
                                            """))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자 정보 없음",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 404,
                                                "name": "USER_NOT_FOUND",
                                                "message": "인증된 사용자 정보를 찾을 수 없습니다.",
                                                "errors": null
                                            }
                                            """)))
            })
    ResponseEntity<UserInfoResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자를 탈퇴(Soft Delete) 처리합니다. <br><br>" +
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
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 401,
                                                "name": "JWT_ENTRY_POINT",
                                                "message": "인증되지 않은 사용자입니다.",
                                                "errors": null
                                            }
                                            """))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자 정보 없음",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 404,
                                                "name": "USER_NOT_FOUND",
                                                "message": "인증된 사용자 정보를 찾을 수 없습니다.",
                                                "errors": null
                                            }
                                            """)))
            })
    ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response);
}