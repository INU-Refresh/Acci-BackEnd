package refresh.acci.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 오류가 발생했습니다."),
    MISSING_PART(HttpStatus.BAD_REQUEST, 400, "요청에 필요한 부분이 없습니다."),
    NO_HANDLER_FOUND(HttpStatus.NOT_FOUND, 404, "요청하신 API가 존재하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 415, "지원하지 않는 Content-Type입니다."),

    // Jwt & Auth
    JWT_ENTRY_POINT(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 사용자입니다."),
    JWT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "리소스에 접근할 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, 401, "인증된 사용자 정보를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 토큰입니다."),
    INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, 401, "유효하지 않거나 만료된 인증 코드입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 401, "리프레시 토큰이 존재하지 않습니다."),

    // OAuth
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, 400, "Provider 정보가 없습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 OAuth 제공자입니다."),
    DUPLICATE_OAUTH_PROVIDER(HttpStatus.INTERNAL_SERVER_ERROR, 500, "중복된 OAuth 제공자가 등록되어 있습니다."),
    OAUTH_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "OAuth 요청 직렬화에 실패했습니다."),
    OAUTH_DESERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "OAuth 요청 역직렬화에 실패했습니다."),

    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "요청한 값이 올바르지 않습니다."),

    // Analysis
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "분석을 찾을 수 없습니다."),
    AI_SERVER_COMMUNICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "AI 서버와의 통신에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "파일 업로드에 실패했습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 파일 형식입니다."),
    TOO_MANY_ANALYSIS_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 429, "분석 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    VIDEO_FILE_MISSING(HttpStatus.BAD_REQUEST, 400, "업로드할 비디오 파일이 없습니다."),
    ACCIDENT_TYPE_NOT_DETECTED(HttpStatus.BAD_REQUEST, 400, "사고 유형 매핑에 실패했습니다."),
    ANALYSIS_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "분석이 중단되었습니다."),
    ACCESS_DENIED_TO_ANALYSIS(HttpStatus.FORBIDDEN, 403, "해당 분석에 접근할 권한이 없습니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "S3 업로드에 실패했습니다."),
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "분석 영상이 존재하지 않습니다."),

    // Repair Estimate
    INVALID_VEHICLE_BRAND(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 차량 브랜드입니다."),
    INVALID_VEHICLE_TYPE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 차종입니다."),
    INVALID_VEHICLE_SEGMENT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 차급입니다."),
    INVALID_DAMAGE_SEVERITY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 파손 정도입니다."),
    INVALID_REPAIR_METHOD(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 수리 방법입니다."),
    REPAIR_ESTIMATE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "수리비 견적을 찾을 수 없습니다."),
    LLM_API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "LLM API 호출에 실패했습니다."),
    LLM_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "LLM 응답 파싱에 실패했습니다."),
    INVALID_VEHICLE_YEAR(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 연식입니다."),
    DAMAGE_DETAILS_REQUIRED(HttpStatus.BAD_REQUEST, 400, "파손 부위 정보가 필요합니다."),
    TOO_MANY_DAMAGE_DETAILS(HttpStatus.BAD_REQUEST, 400, "파손 부위는 최대 20개까지 입력 가능합니다."),
    REPAIR_ESTIMATE_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,400, "이미지는 최대 5장까지 업로드 가능합니다."),

    ;

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}