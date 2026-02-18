package refresh.acci.domain.analysis.application.port.out;

import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiAnalyzeResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiStatusResponse;

import java.nio.file.Path;

public interface AiClientPort {
    AiAnalyzeResponse requestAnalysis(Path videoPath);
    AiStatusResponse getStatus(String jobId);
    AiResultResponse getResult(String jobId);
}
