package refresh.acci.domain.repair.infra.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class RepairSseEmitterManager {

    private static final long SSE_TIMEOUT = 10 * 60 * 1000L; //10분

    private final ConcurrentHashMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();


    /**
     * SSE Emitter 생성 및 등록
     */
    public SseEmitter create(UUID estimateId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(estimateId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.remove(estimateId));
        emitter.onTimeout(() -> emitters.remove(estimateId));
        emitter.onError(error -> emitters.remove(estimateId));

        log.debug("SSE 구독 등록 - estimateId: {}", estimateId);
        return emitter;
    }

    /**
     * estimateId의 모든 구독자에게 이벤트 전송
     */
    public void send(UUID estimateId, String eventName, Object data) {
        List<SseEmitter> emitterList = emitters.getOrDefault(estimateId, List.of());

        for (SseEmitter emitter : emitterList) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data)
                );
            } catch (IOException e) {
                log.warn("SSE 전송 실패 - estimateId: {}", estimateId);
                remove(estimateId, emitter);
            }
        }
    }

    /**
     * estimateId의 모든 구독자 연결 종료
     */
    public void complete(UUID estimateId) {
        List<SseEmitter> emitterList = emitters.remove(estimateId);
        if (emitterList == null) return;

        for (SseEmitter emitter : emitterList) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("SSE complete 실패 - estimateId: {}", estimateId);
            }
        }
        log.debug("SSE 연결 종료 - estimateId: {}", estimateId);
    }


    private void remove(UUID estimateId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(estimateId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(estimateId);
            }
        }
    }

}
