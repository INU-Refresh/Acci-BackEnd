package refresh.acci.domain.analysis.application.port.out;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

public interface AnalysisEventPort {
    void sendStatus(Analysis analysis);
    SseEmitter subscribe(UUID analysisId);
}
