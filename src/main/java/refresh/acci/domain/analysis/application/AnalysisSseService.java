package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class AnalysisSseService {

    private final AnalysisQueryService analysisQueryService;
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분

    // SSE 구독
    public SseEmitter subscribe(UUID analysisId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // 구독자 목록에 추가
        emitters.computeIfAbsent(analysisId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 구독 종료 시 목록에서 제거
        emitter.onCompletion(() -> removeEmitter(analysisId, emitter));
        emitter.onTimeout(() -> removeEmitter(analysisId, emitter));
        emitter.onError((e) -> removeEmitter(analysisId, emitter));

        Analysis analysis = analysisQueryService.getAnalysis(analysisId);

        // 구독 성공 메시지(현 상태) 전송
        try {
            emitter.send(SseEmitter.event().name("status").data(Map.of(
                    "analysisId", analysis.getId(),
                    "status", analysis.getAnalysisStatus(),
                    "isCompleted", analysis.isCompleted())));
        } catch (Exception e) {
            removeEmitter(analysisId, emitter);
        }

        return emitter;
    }

    // 이벤트 전송
    public void send(UUID analysisId, String eventName, Object data) {
        // 해당 분석 ID에 대한 모든 구독자에게 이벤트 전송
        List<SseEmitter> list = emitters.getOrDefault(analysisId, List.of());
        for (SseEmitter emitter : list) {
            try {
                // 이벤트 전송
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                // 전송 실패 시 구독자 목록에서 제거
                removeEmitter(analysisId, emitter);
            }
        }
    }

    // 구독자 제거
    private void removeEmitter(UUID analysisId, SseEmitter emitter) {
        var list = emitters.get(analysisId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(analysisId);
        }
    }
}
