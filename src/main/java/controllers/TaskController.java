package controllers;

import backend.models.task.Priority;
import backend.models.task.Task;
import backend.security.AuthInterceptor;
import backend.services.TaskService;
import controllers.ApiDtos.MoveRequest;
import controllers.ApiDtos.TaskDto;
import controllers.ApiDtos.TaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/** Cards. The access checks (EDIT on the board) are done by TaskService; a refusal becomes 403. */
@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService tasks;

    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    @PostMapping("/columns/{columnId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@PathVariable("columnId") UUID columnId,
                          @RequestBody TaskRequest body,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Task created = tasks.createTask(body.title(), body.description(), parsePriority(body.priority()),
                body.tag(), body.dueDate(), body.assigneeId(), columnId, userId);
        return TaskDto.of(created);
    }

    /** Replaces title, description, priority, tag and due date. The column is changed by /move, not here. */
    @PutMapping("/tasks/{id}")
    public TaskDto update(@PathVariable("id") UUID id,
                          @RequestBody TaskRequest body,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Task changes = new Task(body.title(), body.description());
        changes.setId(id);
        changes.setPriority(parsePriority(body.priority())); // null = keep the old one
        changes.setTag(body.tag());
        changes.setDueDate(body.dueDate());
        return TaskDto.of(tasks.updateTask(changes, userId));
    }

    @PutMapping("/tasks/{id}/move")
    public TaskDto move(@PathVariable("id") UUID id,
                        @RequestBody MoveRequest body,
                        @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        int position = body.position() != null ? body.position() : -1; // -1 = to the end of the column
        return TaskDto.of(tasks.moveTask(id, body.columnId(), position, userId));
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id,
                       @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        if (!tasks.deleteTask(id, userId)) {
            throw new NoSuchElementException("Task not found with id: " + id);
        }
    }

    private static Priority parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Priority.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown priority '" + value + "', use LOW, MEDIUM or HIGH");
        }
    }
}
