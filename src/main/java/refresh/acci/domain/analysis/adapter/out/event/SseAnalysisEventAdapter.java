package refresh.acci.domain.analysis.adapter.out.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class SseAnalysisEventAdapter implements AnalysisEventPort {

    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분
    private final ConcurrentHashMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // SSE 구독
    @Override
    public SseEmitter subscribe(UUID analysisId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // 구독자 목록에 추가
        emitters.computeIfAbsent(analysisId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 구독 종료 시 목록에서 제거
        emitter.onCompletion(() -> removeEmitter(analysisId, emitter));
        emitter.onTimeout(() -> removeEmitter(analysisId, emitter));
        emitter.onError((e) -> removeEmitter(analysisId, emitter));

        return emitter;
    }

    @Override
    public void sendStatus(Analysis analysis) {
        List<SseEmitter> list = emitters.getOrDefault(analysis.getId(), List.of());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("status")
                                .data(Map.of(
                                        "analysisId", analysis.getId(),
                                        "status", analysis.getAnalysisStatus(),
                                        "isCompleted", analysis.isCompleted()))
                );
            } catch (Exception e) {
                removeEmitter(analysis.getId(), emitter);
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
