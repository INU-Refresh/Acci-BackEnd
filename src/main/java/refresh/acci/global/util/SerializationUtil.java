package refresh.acci.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.Base64;

@Slf4j
public class SerializationUtil {

    public static String serialize(Object object) {
        try {
            byte[] serialized = SerializationUtils.serialize(object);
            return Base64.getUrlEncoder().encodeToString(serialized);
        } catch (Exception e) {
            log.error("OAuth 요청 직렬화 실패: {}", object.getClass().getName(), e);
            throw new CustomException(ErrorCode.OAUTH_SERIALIZATION_FAILED);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T> T deserialize(String value, Class<T> cls) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            Object deserialized = SerializationUtils.deserialize(bytes);
            return cls.cast(deserialized);
        } catch (Exception e) {
            log.error("OAuth 요청 역직렬화 실패: {}", cls.getName(), e);
            throw new CustomException(ErrorCode.OAUTH_DESERIALIZATION_FAILED);
        }
    }
}