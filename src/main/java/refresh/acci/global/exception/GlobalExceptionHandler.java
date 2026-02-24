package refresh.acci.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException ex) {
        return ErrorResponseEntity.toResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ErrorResponseEntity.toResponseEntity(errorCode, "요청한 값이 올바르지 않습니다.", errors);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseEntity> handleMissingPart(MissingServletRequestPartException ex) {
        ErrorCode errorCode = ErrorCode.MISSING_PART;
        String message = "요청에 필요한 부분이 없습니다: " + ex.getRequestPartName();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponseEntity> handleMissingCookie(MissingRequestCookieException ex) {
        ErrorCode errorCode = ErrorCode.REFRESH_TOKEN_NOT_FOUND;
        String message = "필수 쿠키가 누락되었습니다: " + ex.getCookieName();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseEntity> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NO_HANDLER_FOUND;
        String message = "존재하지 않는 API입니다: " + ex.getRequestURL();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        String message = "지원하지 않는 HTTP 메서드입니다: " + ex.getMethod();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseEntity> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        String message = "지원하지 않는 Content-Type입니다: " + ex.getContentType();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleException(Exception ex) {
        log.error("예상치 못한 예외", ex);
        return ErrorResponseEntity.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}