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

    // Jwt & Auth
    JWT_ENTRY_POINT(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 사용자입니다."),
    JWT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "리소스에 접근할 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, 401, "인증된 사용자 정보를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 401, "리프레시 토큰이 존재하지 않습니다."),

    //OAuth
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, 400, "Provider 정보가 없습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 OAuth 제공자입니다."),
    DUPLICATE_OAUTH_PROVIDER(HttpStatus.INTERNAL_SERVER_ERROR, 500, "중복된 OAuth 제공자가 등록되어 있습니다."),

    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "요청한 값이 올바르지 않습니다."),

    // Analysis
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "분석을 찾을 수 없습니다."),
    AI_SERVER_COMMUNICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "AI 서버와의 통신에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "파일 업로드에 실패했습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 파일 형식입니다.")

    ;

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}