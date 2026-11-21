package fr.miage.blindbattle.messaging;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseBroadcaster {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String gameCode) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(gameCode, code -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(gameCode, emitter));
        emitter.onTimeout(() -> remove(gameCode, emitter));
        emitter.onError(error -> remove(gameCode, emitter));
        return emitter;
    }

    public void broadcast(String gameCode, String eventId, String name, String json) {
        emitters.getOrDefault(gameCode, List.of())
                .forEach(emitter -> send(gameCode, emitter, SseEmitter.event().id(eventId).name(name).data(json, MediaType.APPLICATION_JSON)));
    }

    public int subscribers(String gameCode) { return emitters.getOrDefault(gameCode, List.of()).size(); }

    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
        emitters.forEach((code, list) -> list.forEach(emitter -> send(code, emitter, SseEmitter.event().name("ping").data("ping"))));
    }

    private void send(String gameCode, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException error) {
            remove(gameCode, emitter);
        }
    }

    private void remove(String gameCode, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(gameCode);
        if (list != null) list.remove(emitter);
    }
}
