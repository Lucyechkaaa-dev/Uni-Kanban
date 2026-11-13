package backend.services;

import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Pushes board changes to the browsers that have the board open. Each event is a {@link BoardChange}
 * (JSON) under the name "change". Subscribers get "ready" first, then changes, then keep-alive comments.
 *
 * Kept in memory, so it works for one running backend (which is how this app runs).
 */
@Log4j2
@Component
public class BoardEvents {

    private static final long CONNECTION_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);
    private static final String CHANGE = "change";
    private static final String READY = "ready";

    private final Map<UUID, Set<SseEmitter>> subscribers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "board-events-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    public BoardEvents() {
        // a comment line now and then keeps idle connections open through proxies
        heartbeat.scheduleAtFixedRate(this::sendHeartbeat, 25, 25, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe(UUID boardId) {
        SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT_MS);
        subscribers.computeIfAbsent(boardId, id -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> unsubscribe(boardId, emitter));
        emitter.onTimeout(() -> unsubscribe(boardId, emitter));
        emitter.onError(e -> unsubscribe(boardId, emitter));
        send(boardId, emitter, SseEmitter.event().name(READY).data("ok"));
        return emitter;
    }

    /** Tells everyone watching the board about a change. Safe to call from any thread. */
    public void publish(UUID boardId, BoardChange change) {
        if (boardId == null || change == null) return;
        Set<SseEmitter> watchers = subscribers.get(boardId);
        if (watchers == null) return;
        for (SseEmitter emitter : watchers) {
            send(boardId, emitter, SseEmitter.event().name(CHANGE).data(change, MediaType.APPLICATION_JSON));
        }
    }

    private void sendHeartbeat() {
        try {
            for (Map.Entry<UUID, Set<SseEmitter>> entry : subscribers.entrySet()) {
                for (SseEmitter emitter : entry.getValue()) {
                    send(entry.getKey(), emitter, SseEmitter.event().comment("keepalive"));
                }
            }
        } catch (RuntimeException e) {
            log.warn("Heartbeat failed", e); // never let one failure stop the schedule
        }
    }

    private void send(UUID boardId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            // the browser went away or the stream already ended: forget this connection
            unsubscribe(boardId, emitter);
        }
    }

    private void unsubscribe(UUID boardId, SseEmitter emitter) {
        Set<SseEmitter> watchers = subscribers.get(boardId);
        if (watchers != null) {
            watchers.remove(emitter);
            if (watchers.isEmpty()) subscribers.remove(boardId, watchers);
        }
    }

    @PreDestroy
    void shutdown() {
        heartbeat.shutdownNow();
    }
}
