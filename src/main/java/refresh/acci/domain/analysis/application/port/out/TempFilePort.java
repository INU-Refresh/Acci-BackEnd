package refresh.acci.domain.analysis.application.port.out;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface TempFilePort {
    Path saveToTempFile(MultipartFile file, UUID analysisId);
    void deleteTempFile(Path path);
}
