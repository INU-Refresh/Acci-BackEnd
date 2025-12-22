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

    // Jwt
    JWT_ENTRY_POINT(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 사용자입니다."),
    JWT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "리소스에 접근할 권한이 없습니다."),

    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "요청한 값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}