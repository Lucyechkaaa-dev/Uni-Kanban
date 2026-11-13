package controllers;

import backend.security.AuthInterceptor;
import backend.services.Access;
import backend.services.BoardEvents;
import backend.services.TaskService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.NoSuchElementException;
import java.util.UUID;

/** A live stream per board: "change" whenever someone else (or you) changes it. Needs at least view access. */
@RestController
@RequestMapping("/api/boards")
public class BoardEventsController {

    private final TaskService tasks;
    private final BoardEvents events;

    public BoardEventsController(TaskService tasks, BoardEvents events) {
        this.tasks = tasks;
        this.events = events;
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable("id") UUID id,
                             @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        if (tasks.accessLevel(id, userId) == Access.NONE) {
            throw new NoSuchElementException("Board not found with id: " + id);
        }
        return events.subscribe(id);
    }
}
