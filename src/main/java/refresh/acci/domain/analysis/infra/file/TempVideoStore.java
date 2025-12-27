package refresh.acci.domain.analysis.infra.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class TempVideoStore {

    // MultipartFile을 임시 파일로 저장 후 경로 반환
    public Path saveToTempFile(MultipartFile video, UUID analysisId) {
        try {
            // 파일 확장자 추출
            String original = video.getOriginalFilename();
            String extension = getFileExtension(original);

            // mp4, avi 확장자만 허용
            if (!extension.equals("mp4") && !extension.equals("avi")) {
                throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
            }

            // OS 임시 디렉토리에 임시 파일 생성
            Path tempFilePath = Files.createTempFile("analysis-" + analysisId + "-", "." + extension);

            // 임시 파일 경로에 파일 복사, 존재 시 덮어씀 (try-with-resources)
            try (var fileInputStream = video.getInputStream()) {
                Files.copy(fileInputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFilePath;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 임시 파일 삭제
    public void deleteFile(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("임시 파일 삭제 실패: {} {}", path, e.getMessage());
        }
    }

    // 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() -1) return "";
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
